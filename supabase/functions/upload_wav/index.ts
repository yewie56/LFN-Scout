import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { buildStoragePath } from "./storage_filename.js";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

async function sha256Hex(value: string): Promise<string> {
  const data = new TextEncoder().encode(value);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return Array.from(new Uint8Array(digest))
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

function serviceRoleKey(): string | null {
  const direct = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  const secretKeys = Deno.env.get("SUPABASE_SECRET_KEYS");
  if (secretKeys) {
    try {
      const parsed = JSON.parse(secretKeys);
      if (
        parsed &&
        typeof parsed === "object" &&
        typeof parsed.default === "string" &&
        parsed.default.trim()
      ) {
        return parsed.default.trim();
      }
    } catch {
      if (secretKeys.trim()) return secretKeys.trim();
    }
  }
  return direct?.trim() || null;
}

function decodeBase64(value: string): Uint8Array {
  const binary = atob(value);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (req.method === "GET") {
    return jsonResponse({
      ok: true,
      service: "upload_wav",
      version: "1.5.0",
    });
  }
  if (req.method !== "POST") {
    return jsonResponse({ error: "method_not_allowed" }, 405);
  }

  try {
    const body = await req.json();
    const key = typeof body?.key === "string" ? body.key.trim() : "";
    const recordingId =
      typeof body?.recording_id === "string" ? body.recording_id.trim() : "";
    const recordedAt =
      typeof body?.recorded_at === "string" ? body.recorded_at : "";
    const wavBase64 =
      typeof body?.wav_base64 === "string" ? body.wav_base64 : "";

    const sampleRateHz = Number(body?.sample_rate_hz);
    const channels = Number(body?.channels);
    const bitsPerSample = Number(body?.bits_per_sample);
    const gainDb = Number(body?.gain_db);
    const clippedSamples = Number(body?.clipped_samples);
    const comment =
      body?.comment == null
        ? null
        : typeof body.comment === "string"
          ? body.comment
          : "__INVALID_COMMENT__";
    const sourceCode =
      typeof body?.audio_source === "string"
        ? body.audio_source.trim().toUpperCase()
        : "";
    const hasLatitude = body?.latitude !== undefined && body?.latitude !== null;
    const hasLongitude = body?.longitude !== undefined && body?.longitude !== null;
    const requestLatitude = hasLatitude ? Number(body.latitude) : null;
    const requestLongitude = hasLongitude ? Number(body.longitude) : null;
    const requestAccuracy = body?.location_accuracy_m == null ? null : Number(body.location_accuracy_m);
    const requestLocationTimestamp =
      typeof body?.location_timestamp === "string" ? body.location_timestamp : null;
    const requestLocationSource =
      typeof body?.location_source === "string" && body.location_source.trim()
        ? body.location_source.trim().slice(0, 80)
        : null;
    const captureOrigin =
      typeof body?.capture_origin === "string" && body.capture_origin.trim()
        ? body.capture_origin.trim().toUpperCase()
        : "LEGACY_UNKNOWN";
    const triggerType =
      typeof body?.trigger_type === "string" && body.trigger_type.trim()
        ? body.trigger_type.trim().toUpperCase()
        : "LEGACY_UNKNOWN";
    const triggerMetadata =
      body?.trigger_metadata == null
        ? {}
        : typeof body.trigger_metadata === "object" && !Array.isArray(body.trigger_metadata)
          ? body.trigger_metadata
          : null;

    if (
      !key ||
      !recordingId ||
      !recordedAt ||
      !wavBase64 ||
      sampleRateHz !== 1000 ||
      channels !== 1 ||
      bitsPerSample !== 16 ||
      !Number.isInteger(gainDb) ||
      gainDb < 0 ||
      gainDb > 50 ||
      !["UNP", "MIC"].includes(sourceCode) ||
      comment === "__INVALID_COMMENT__" ||
      (typeof comment === "string" && comment.length > 2000) ||
      hasLatitude !== hasLongitude ||
      (hasLatitude && (!Number.isFinite(requestLatitude) || requestLatitude! < -90 || requestLatitude! > 90)) ||
      (hasLongitude && (!Number.isFinite(requestLongitude) || requestLongitude! < -180 || requestLongitude! > 180)) ||
      (requestAccuracy != null && (!Number.isFinite(requestAccuracy) || requestAccuracy < 0)) ||
      !["OBSERVER", "ANALYZER", "LEGACY_UNKNOWN"].includes(captureOrigin) ||
      !["MANUAL", "ROLLING_LEVEL", "ANALYZER_LEVEL", "HARDWARE_EXTERNAL", "LEGACY_UNKNOWN"].includes(triggerType) ||
      triggerMetadata == null
    ) {
      return jsonResponse({ error: "invalid_audio_request" }, 400);
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceKey = serviceRoleKey();
    if (!supabaseUrl || !serviceKey) {
      return jsonResponse({ error: "server_configuration_error" }, 500);
    }

    const supabase = createClient(supabaseUrl, serviceKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    });

    const keyHash = await sha256Hex(key);
    const { data: observer, error: observerError } = await supabase
      .from("observers")
      .select("observer_id, enabled, status, latitude, longitude")
      .eq("key_hash", keyHash)
      .maybeSingle();

    if (observerError) {
      console.error(observerError);
      return jsonResponse({ error: "observer_lookup_failed" }, 500);
    }
    if (!observer || observer.enabled !== true) {
      return jsonResponse({ error: "invalid_or_disabled_key" }, 401);
    }
    if (observer.status === "revoked") {
      return jsonResponse({ error: "key_revoked" }, 403);
    }
    if (observer.status === "paused") {
      return jsonResponse({ error: "key_paused" }, 403);
    }

    const wav = decodeBase64(wavBase64);
    if (
      wav.length < 44 ||
      wav.length > 1024 * 1024 ||
      new TextDecoder().decode(wav.slice(0, 4)) !== "RIFF"
    ) {
      return jsonResponse({ error: "invalid_wav" }, 400);
    }

    const recordedAtDate = new Date(recordedAt);
    if (Number.isNaN(recordedAtDate.getTime())) {
      return jsonResponse({ error: "invalid_recorded_at" }, 400);
    }

    if (requestLocationTimestamp) {
      const parsedLocationTimestamp = new Date(requestLocationTimestamp);
      if (Number.isNaN(parsedLocationTimestamp.getTime())) {
        return jsonResponse({ error: "invalid_location_timestamp" }, 400);
      }
    }

    const observerLatitude = Number(observer.latitude);
    const observerLongitude = Number(observer.longitude);
    const observerBaseAvailable =
      Number.isFinite(observerLatitude) && Number.isFinite(observerLongitude) &&
      !(observerLatitude === 0 && observerLongitude === 0);

    const location = hasLatitude
      ? {
          latitude: requestLatitude,
          longitude: requestLongitude,
          accuracy: requestAccuracy,
          timestamp: requestLocationTimestamp ?? recordedAtDate.toISOString(),
          source: requestLocationSource ?? "ANDROID_EVENT",
          status: "VALID_LIVE",
        }
      : observerBaseAvailable
        ? {
            latitude: observerLatitude,
            longitude: observerLongitude,
            accuracy: null,
            timestamp: null,
            source: "OBSERVER_BASE",
            status: "FALLBACK_OBSERVER_BASE",
          }
        : {
            latitude: null,
            longitude: null,
            accuracy: null,
            timestamp: null,
            source: "NONE",
            status: "UNAVAILABLE",
          };

    const ratingWindowStart =
      new Date(recordedAtDate.getTime() - 30 * 60 * 1000).toISOString();

    const ratingWindowEnd =
      recordedAtDate.toISOString();

    const { data: latestRating, error: ratingError } = await supabase
      .from("observations")
      .select("rating, response_time")
      .eq("observer_id", observer.observer_id)
      .gte("rating", 0)
      .lte("rating", 5)
      .not("response_time", "is", null)
      .gte("response_time", ratingWindowStart)
      .lte("response_time", ratingWindowEnd)
      .order("response_time", { ascending: false })
      .limit(1)
      .maybeSingle();

    if (ratingError) {
      console.error(ratingError);
      return jsonResponse({ error: "rating_lookup_failed" }, 500);
    }

    const ratingForFilename =
      latestRating &&
      Number.isInteger(latestRating.rating) &&
      latestRating.rating >= 0 &&
      latestRating.rating <= 5
        ? latestRating.rating
        : null;

    const path = buildStoragePath({
      observerId: observer.observer_id,
      recordedAt,
      gainDb,
      sourceCode,
      rating: ratingForFilename,
    });

    const { error: uploadError } = await supabase.storage
      .from("brumm-audio")
      .upload(path, wav, {
        contentType: "audio/wav",
        upsert: false,
      });

    if (uploadError) {
      console.error(uploadError);
      return jsonResponse({ error: "wav_storage_failed" }, 500);
    }

    const { error: insertError } = await supabase
      .from("audio_recordings")
      .insert({
        recording_id: recordingId,
        observer_id: observer.observer_id,
        recorded_at: recordedAt,
        sample_rate_hz: sampleRateHz,
        channels,
        bits_per_sample: bitsPerSample,
        gain_db: gainDb,
        clipped_samples: Math.max(0, Math.floor(clippedSamples || 0)),
        comment:
          typeof comment === "string" && comment.trim()
            ? comment.slice(0, 2000)
            : null,
        storage_path: path,
        latitude: location.latitude,
        longitude: location.longitude,
        location_accuracy_m: location.accuracy,
        location_timestamp: location.timestamp,
        location_source: location.source,
        location_status: location.status,
        capture_origin: captureOrigin,
        trigger_type: triggerType,
        trigger_metadata: triggerMetadata,
      });

    if (insertError) {
      console.error(insertError);
      await supabase.storage.from("brumm-audio").remove([path]);
      return jsonResponse({ error: "audio_metadata_insert_failed" }, 500);
    }

    return jsonResponse({
      ok: true,
      recording_id: recordingId,
      storage_path: path,
    });
  } catch (error) {
    console.error(error);
    return jsonResponse({ error: "invalid_request" }, 400);
  }
});
