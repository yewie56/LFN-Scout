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

type Point = {
  latitude: number;
  longitude: number;
};

function numberValue(value: unknown): number | null {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === "string" && value.trim()) {
    const parsed = Number(value);

    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }

  return null;
}

function validPoint(
  latitude: number | null,
  longitude: number | null,
  zeroZeroMeansUnavailable = false,
): Point | null {
  if (
    latitude === null ||
    longitude === null ||
    latitude < -90 ||
    latitude > 90 ||
    longitude < -180 ||
    longitude > 180
  ) {
    return null;
  }

  if (
    zeroZeroMeansUnavailable &&
    latitude === 0 &&
    longitude === 0
  ) {
    return null;
  }

  return {
    latitude,
    longitude,
  };
}

function readBasisLocation(row: Record<string, unknown>): Point | null {
  /*
   * Die Historie des LFN-Scout-Projekts legt einen getrennten
   * registrierten/Basisstandort fest. Da ältere Installationen bei der
   * Spaltenbenennung variieren können, werden mehrere gebräuchliche Paare
   * akzeptiert. Die Setup-Datei nennt die bevorzugten Namen.
   */
  const candidates = [
    ["base_latitude", "base_longitude"],
    ["basis_latitude", "basis_longitude"],
    ["registered_latitude", "registered_longitude"],
    ["home_latitude", "home_longitude"],
    ["location_latitude", "location_longitude"],
    ["latitude", "longitude"],
  ];

  for (const [latName, lonName] of candidates) {
    const point = validPoint(
      numberValue(row[latName]),
      numberValue(row[lonName]),
      true,
    );

    if (point) {
      return point;
    }
  }

  return null;
}

function haversineKm(a: Point, b: Point): number {
  const earthRadiusKm = 6371.0088;
  const toRad = (degrees: number) => degrees * Math.PI / 180;

  const lat1 = toRad(a.latitude);
  const lat2 = toRad(b.latitude);
  const deltaLat = toRad(b.latitude - a.latitude);
  const deltaLon = toRad(b.longitude - a.longitude);

  const sinLat = Math.sin(deltaLat / 2);
  const sinLon = Math.sin(deltaLon / 2);

  const h =
    sinLat * sinLat +
    Math.cos(lat1) *
      Math.cos(lat2) *
      sinLon *
      sinLon;

  return 2 * earthRadiusKm * Math.asin(Math.sqrt(h));
}

function alignedWindow(nowMs: number): {
  startMs: number;
  endMs: number;
} {
  const halfHourMs = 30 * 60 * 1000;
  const twoHoursMs = 4 * halfHourMs;

  /*
   * Aktuellen 30-Minuten-Block vollständig darstellen:
   * z.B. 12:20 -> Fensterende 12:30, Fensterstart 10:30.
   * Da deutsche Zeitzonen um volle Stunden von UTC abweichen, liegen
   * :00/:30-Blöcke in UTC und lokaler Zeit auf denselben Minutengrenzen.
   */
  const endMs =
    Math.ceil(nowMs / halfHourMs) * halfHourMs;

  return {
    startMs: endMs - twoHoursMs,
    endMs,
  };
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", {
      headers: corsHeaders,
    });
  }

  if (req.method === "GET") {
    return jsonResponse({
      ok: true,
      service: "get_radius_histograms",
      version: "1.0.0",
    });
  }

  if (req.method !== "POST") {
    return jsonResponse(
      {
        error: "method_not_allowed",
      },
      405,
    );
  }

  try {
    const body = await req.json();

    const key =
      typeof body?.key === "string"
        ? body.key.trim()
        : "";

    if (!key) {
      return jsonResponse(
        {
          error: "missing_key",
        },
        400,
      );
    }

    const radiusRaw =
      numberValue(body?.radius_km) ?? 600;

    const radiusKm =
      Math.max(
        1,
        Math.min(
          20000,
          Math.round(radiusRaw),
        ),
      );

    const requestPoint =
      validPoint(
        numberValue(body?.latitude),
        numberValue(body?.longitude),
        false,
      );

    const supabaseUrl =
      Deno.env.get("SUPABASE_URL");

    const serviceKey =
      serviceRoleKey();

    if (!supabaseUrl || !serviceKey) {
      console.error(
        "Missing Supabase server environment variables.",
      );

      return jsonResponse(
        {
          error: "server_configuration_error",
        },
        500,
      );
    }

    const supabase =
      createClient(
        supabaseUrl,
        serviceKey,
        {
          auth: {
            persistSession: false,
            autoRefreshToken: false,
          },
        },
      );

    const keyHash =
      await sha256Hex(key);

    const {
      data: observer,
      error: observerError,
    } =
      await supabase
        .from("observers")
        .select("*")
        .eq("key_hash", keyHash)
        .maybeSingle();

    if (observerError) {
      console.error(
        "Observer lookup failed:",
        observerError,
      );

      return jsonResponse(
        {
          error: "observer_lookup_failed",
        },
        500,
      );
    }

    if (
      !observer ||
      observer.enabled !== true
    ) {
      return jsonResponse(
        {
          error: "invalid_or_disabled_key",
        },
        401,
      );
    }
    if (observer.status === "revoked") {
      return jsonResponse({ error: "key_revoked" }, 403);
    }
    if (observer.status === "paused") {
      return jsonResponse({ error: "key_paused" }, 403);
    }

    let center: Point;
    let centerSource: "gps" | "basis";

    if (requestPoint) {
      center = requestPoint;
      centerSource = "gps";
    } else {
      const basis =
        readBasisLocation(
          observer as Record<string, unknown>,
        );

      if (!basis) {
        return jsonResponse(
          {
            error: "basis_location_unavailable",
            expected_basis_columns: [
              "base_latitude/base_longitude",
              "basis_latitude/basis_longitude",
              "registered_latitude/registered_longitude",
              "home_latitude/home_longitude",
              "location_latitude/location_longitude",
              "latitude/longitude",
            ],
          },
          422,
        );
      }

      center = basis;
      centerSource = "basis";
    }

    const window =
      alignedWindow(Date.now());

    const {
      data: observations,
      error: observationsError,
    } =
      await supabase
        .from("observations")
        .select(
          "observer_id,rating,comment,scheduled_time,response_time,latitude,longitude",
        )
        .gte(
          "scheduled_time",
          new Date(window.startMs).toISOString(),
        )
        .lt(
          "scheduled_time",
          new Date(window.endMs).toISOString(),
        )
        .order(
          "scheduled_time",
          {
            ascending: true,
          },
        )
        .range(0, 4999);

    if (observationsError) {
      console.error(
        "Observation radius query failed:",
        observationsError,
      );

      return jsonResponse(
        {
          error: "radius_observations_query_failed",
        },
        500,
      );
    }

    const rows =
      observations ?? [];

    const observerIds =
      Array.from(
        new Set(
          rows
            .map((row) => row.observer_id)
            .filter(
              (value) =>
                typeof value === "string" &&
                value.length > 0,
            ),
        ),
      );

    const basisByObserver =
      new Map<string, Point>();

    if (observerIds.length > 0) {
      const {
        data: basisRows,
        error: basisRowsError,
      } =
        await supabase
          .from("observers")
          .select("*")
          .in(
            "observer_id",
            observerIds,
          );

      if (basisRowsError) {
        console.error(
          "Observer basis lookup failed:",
          basisRowsError,
        );

        return jsonResponse(
          {
            error: "observer_basis_lookup_failed",
          },
          500,
        );
      }

      for (const row of basisRows ?? []) {
        const observerId =
          typeof row.observer_id === "string"
            ? row.observer_id
            : "";

        const basis =
          readBasisLocation(
            row as Record<string, unknown>,
          );

        if (observerId && basis) {
          basisByObserver.set(
            observerId,
            basis,
          );
        }
      }
    }

    const halfHourMs =
      30 * 60 * 1000;

    const blocks =
      Array.from(
        {
          length: 4,
        },
        (_, index) => ({
          startMs:
            window.startMs +
            index * halfHourMs,
          endMs:
            window.startMs +
            (index + 1) * halfHourMs,
          counts: [0, 0, 0, 0, 0, 0],
          noResponse: 0,
        }),
      );

    const includedObserverIds =
      new Set<string>();

    const includedObservations: Array<{
      observer_id: string;
      rating: number;
      comment: string | null;
      scheduled_time: string;
      response_time: string | null;
      latitude: number;
      longitude: number;
    }> = [];

    let observationCount = 0;

    for (const row of rows) {
      const scheduledMs =
        Date.parse(row.scheduled_time);

      if (!Number.isFinite(scheduledMs)) {
        continue;
      }

      const observerId =
        typeof row.observer_id === "string"
          ? row.observer_id
          : "";

      const storedLatitude =
        numberValue(row.latitude) ?? 0.0;

      const storedLongitude =
        numberValue(row.longitude) ?? 0.0;

      let observationPoint =
        validPoint(
          storedLatitude,
          storedLongitude,
          true,
        );

      if (!observationPoint && observerId) {
        observationPoint =
          basisByObserver.get(observerId) ??
          null;
      }

      if (!observationPoint) {
        continue;
      }

      if (
        haversineKm(
          center,
          observationPoint,
        ) > radiusKm
      ) {
        continue;
      }

      const blockIndex =
        Math.floor(
          (
            scheduledMs -
            window.startMs
          ) / halfHourMs,
        );

      if (
        blockIndex < 0 ||
        blockIndex >= blocks.length
      ) {
        continue;
      }

      const rating =
        Number(row.rating);

      if (
        !Number.isInteger(rating) ||
        rating < -1 ||
        rating > 5
      ) {
        continue;
      }

      if (rating === -1) {
        blocks[blockIndex].noResponse += 1;
      } else {
        blocks[blockIndex].counts[rating] += 1;
      }

      observationCount += 1;

      if (observerId) {
        includedObserverIds.add(observerId);
      }

      includedObservations.push({
        observer_id: observerId,
        rating,
        comment:
          typeof row.comment === "string" &&
          row.comment.trim()
            ? row.comment
            : null,
        scheduled_time: row.scheduled_time,
        response_time:
          typeof row.response_time === "string"
            ? row.response_time
            : null,
        /*
         * Rohdaten bleiben Rohdaten:
         * 0.0/0.0 bedeutet weiterhin "GPS nicht verfügbar".
         * Der Basisstandort wird nur intern für die Radiusentscheidung
         * verwendet und ersetzt nicht die gespeicherte Messposition.
         */
        latitude: storedLatitude,
        longitude: storedLongitude,
      });
    }

    return jsonResponse({
      ok: true,
      center_source: centerSource,
      radius_km: radiusKm,
      observer_count:
        includedObserverIds.size,
      observation_count:
        observationCount,
      blocks:
        blocks.map((block) => ({
          start_time:
            new Date(
              block.startMs,
            ).toISOString(),
          end_time:
            new Date(
              block.endMs,
            ).toISOString(),
          counts:
            block.counts,
          no_response:
            block.noResponse,
        })),
      observations:
        includedObservations,
    });
  } catch (error) {
    console.error(
      "Unhandled error:",
      error,
    );

    return jsonResponse(
      {
        error: "invalid_request",
      },
      400,
    );
  }
});
