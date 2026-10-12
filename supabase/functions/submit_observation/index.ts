import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
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
      if (secretKeys.trim()) {
        return secretKeys.trim();
      }
    }
  }

  return direct?.trim() || null;
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method === "GET") {
    return jsonResponse({
      ok: true,
      service: "submit_observation",
      version: "1.1.0",
    });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "method_not_allowed" }, 405);
  }

  try {
    const body = await req.json();

    const key =
      typeof body?.key === "string"
        ? body.key.trim()
        : "";

    const eventId =
      typeof body?.event_id === "string"
        ? body.event_id.trim()
        : "";

    const scheduledTime =
      typeof body?.scheduled_time === "string"
        ? body.scheduled_time
        : "";

    const responseTime =
      body?.response_time === null
        ? null
        : (
            typeof body?.response_time === "string"
              ? body.response_time
              : null
          );

    const rating = Number(body?.rating);
    const latitude = Number(body?.latitude);
    const longitude = Number(body?.longitude);

    const appVersion =
      typeof body?.app_version === "string"
        ? body.app_version.trim()
        : "";

    const rawComment =
      typeof body?.comment === "string"
        ? body.comment.trim()
        : "";

    const comment =
      rating === -1 || rawComment.length === 0
        ? null
        : rawComment.slice(0, 2000);

    if (
      !key ||
      !eventId ||
      !scheduledTime ||
      !Number.isInteger(rating) ||
      rating < -1 ||
      rating > 5 ||
      !Number.isFinite(latitude) ||
      !Number.isFinite(longitude)
    ) {
      return jsonResponse(
        { error: "invalid_observation" },
        400,
      );
    }

    if (
      latitude < -90 ||
      latitude > 90 ||
      longitude < -180 ||
      longitude > 180
    ) {
      return jsonResponse(
        { error: "invalid_coordinates" },
        400,
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceKey = serviceRoleKey();

    if (!supabaseUrl || !serviceKey) {
      return jsonResponse(
        { error: "server_configuration_error" },
        500,
      );
    }

    const supabase = createClient(
      supabaseUrl,
      serviceKey,
      {
        auth: {
          persistSession: false,
          autoRefreshToken: false,
        },
      },
    );

    const keyHash = await sha256Hex(key);

    const {
      data: observer,
      error: observerError,
    } =
      await supabase
        .from("observers")
        .select("observer_id, enabled, status")
        .eq("key_hash", keyHash)
        .maybeSingle();

    if (observerError) {
      console.error(
        "Observer lookup failed:",
        observerError,
      );
      return jsonResponse(
        { error: "observer_lookup_failed" },
        500,
      );
    }

    if (!observer || observer.enabled !== true) {
      return jsonResponse(
        { error: "invalid_or_disabled_key" },
        401,
      );
    }
    if (observer.status === "revoked") {
      return jsonResponse({ error: "key_revoked" }, 403);
    }
    if (observer.status === "paused") {
      return jsonResponse({ error: "key_paused" }, 403);
    }

    const {
      data: existing,
      error: existingError,
    } =
      await supabase
        .from("observations")
        .select("event_id")
        .eq("event_id", eventId)
        .maybeSingle();

    if (existingError) {
      console.error(
        "Duplicate lookup failed:",
        existingError,
      );
      return jsonResponse(
        { error: "duplicate_lookup_failed" },
        500,
      );
    }

    if (existing) {
      return jsonResponse({
        ok: true,
        duplicate: true,
        observer_id: observer.observer_id,
        event_id: eventId,
      });
    }

    const {
      error: insertError,
    } =
      await supabase
        .from("observations")
        .insert({
          observer_id: observer.observer_id,
          event_id: eventId,
          scheduled_time: scheduledTime,
          response_time: responseTime,
          rating,
          latitude,
          longitude,
          app_version: appVersion,
          comment,
          wav_present: false,
        });

    if (insertError) {
      console.error(
        "Observation insert failed:",
        insertError,
      );

      return jsonResponse(
        { error: "observation_insert_failed" },
        500,
      );
    }

    return jsonResponse({
      ok: true,
      duplicate: false,
      observer_id: observer.observer_id,
      event_id: eventId,
    });
  } catch (error) {
    console.error("Unhandled error:", error);
    return jsonResponse(
      { error: "invalid_request" },
      400,
    );
  }
});
