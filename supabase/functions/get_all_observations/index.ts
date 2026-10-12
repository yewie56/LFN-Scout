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
      service: "get_all_observations",
      version: "1.0.0",
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

    const offset =
      Math.max(
        0,
        Math.floor(
          Number(body?.offset) || 0,
        ),
      );

    const limit =
      Math.max(
        1,
        Math.min(
          1000,
          Math.floor(
            Number(body?.limit) || 500,
          ),
        ),
      );

    if (!key) {
      return jsonResponse(
        { error: "missing_key" },
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
      data: observations,
      error: observationsError,
    } =
      await supabase
        .from("observations")
        .select(
          "observer_id,event_id,scheduled_time,response_time,rating,comment,latitude,longitude,app_version,wav_present,wav_path",
        )
        .eq(
          "observer_id",
          observer.observer_id,
        )
        .order(
          "scheduled_time",
          { ascending: true },
        )
        .range(
          offset,
          offset + limit,
        );

    if (observationsError) {
      console.error(
        "All observations query failed:",
        observationsError,
      );
      return jsonResponse(
        { error: "all_observations_query_failed" },
        500,
      );
    }

    const rows = observations ?? [];
    const hasMore = rows.length > limit;
    const pageRows =
      hasMore
        ? rows.slice(0, limit)
        : rows;

    return jsonResponse({
      ok: true,
      observer_id: observer.observer_id,
      observations: pageRows,
      has_more: hasMore,
      next_offset: offset + pageRows.length,
    });
  } catch (error) {
    console.error("Unhandled error:", error);
    return jsonResponse(
      { error: "invalid_request" },
      400,
    );
  }
});
