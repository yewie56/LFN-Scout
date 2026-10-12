


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA "vault";






CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA "extensions";





SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."app_config" (
    "config_id" "text" DEFAULT 'default'::"text" NOT NULL,
    "interval_minutes" integer DEFAULT 30 NOT NULL,
    "response_window_minutes" integer DEFAULT 10 NOT NULL,
    "active_from" time without time zone DEFAULT '07:00:00'::time without time zone NOT NULL,
    "active_until" time without time zone DEFAULT '23:00:00'::time without time zone NOT NULL,
    "updated_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "app_config_interval_chk" CHECK ((("interval_minutes" >= 1) AND ("interval_minutes" <= 1440))),
    CONSTRAINT "app_config_response_window_chk" CHECK ((("response_window_minutes" >= 1) AND ("response_window_minutes" <= 1440)))
);


ALTER TABLE "public"."app_config" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."audio_recordings" (
    "recording_id" "uuid" NOT NULL,
    "observer_id" "text" NOT NULL,
    "recorded_at" timestamp with time zone NOT NULL,
    "sample_rate_hz" integer NOT NULL,
    "channels" integer NOT NULL,
    "bits_per_sample" integer NOT NULL,
    "gain_db" integer NOT NULL,
    "clipped_samples" integer DEFAULT 0 NOT NULL,
    "storage_path" "text" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "comment" "text",
    "latitude" double precision,
    "longitude" double precision,
    "location_accuracy_m" double precision,
    "location_timestamp" timestamp with time zone,
    "location_source" "text",
    "location_status" "text" DEFAULT 'LEGACY_UNKNOWN'::"text" NOT NULL,
    "capture_origin" "text" DEFAULT 'LEGACY_UNKNOWN'::"text" NOT NULL,
    "trigger_type" "text" DEFAULT 'LEGACY_UNKNOWN'::"text" NOT NULL,
    "trigger_metadata" "jsonb" DEFAULT '{}'::"jsonb" NOT NULL,
    CONSTRAINT "audio_recordings_capture_origin_chk" CHECK (("capture_origin" = ANY (ARRAY['OBSERVER'::"text", 'ANALYZER'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "audio_recordings_comment_length_check" CHECK ((("comment" IS NULL) OR ("char_length"("comment") <= 2000))),
    CONSTRAINT "audio_recordings_latitude_chk" CHECK ((("latitude" IS NULL) OR (("latitude" >= ('-90'::integer)::double precision) AND ("latitude" <= (90)::double precision)))),
    CONSTRAINT "audio_recordings_location_accuracy_chk" CHECK ((("location_accuracy_m" IS NULL) OR ("location_accuracy_m" >= (0)::double precision))),
    CONSTRAINT "audio_recordings_location_status_chk" CHECK (("location_status" = ANY (ARRAY['VALID_LIVE'::"text", 'FALLBACK_OBSERVER_BASE'::"text", 'UNAVAILABLE'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "audio_recordings_longitude_chk" CHECK ((("longitude" IS NULL) OR (("longitude" >= ('-180'::integer)::double precision) AND ("longitude" <= (180)::double precision)))),
    CONSTRAINT "audio_recordings_trigger_type_chk" CHECK (("trigger_type" = ANY (ARRAY['MANUAL'::"text", 'ROLLING_LEVEL'::"text", 'ANALYZER_LEVEL'::"text", 'HARDWARE_EXTERNAL'::"text", 'LEGACY_UNKNOWN'::"text"])))
);


ALTER TABLE "public"."audio_recordings" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."half_hour_statistics" (
    "bucket_id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "observer_id" "text" NOT NULL,
    "bucket_start" timestamp with time zone NOT NULL,
    "bucket_end" timestamp with time zone NOT NULL,
    "statistics_source" "text" DEFAULT 'ROLLING'::"text" NOT NULL,
    "latitude" double precision,
    "longitude" double precision,
    "location_accuracy_m" double precision,
    "location_timestamp" timestamp with time zone,
    "location_source" "text",
    "location_status" "text" NOT NULL,
    "comment" "text",
    "app_version" "text",
    "range_min_hz" double precision NOT NULL,
    "range_max_hz" double precision NOT NULL,
    "leq_dbfs" double precision,
    "l50_dbfs" double precision,
    "l90_dbfs" double precision,
    "coverage_seconds" integer DEFAULT 0 NOT NULL,
    "valid_epoch_count" integer DEFAULT 0 NOT NULL,
    "quality_status" "text" NOT NULL,
    "lookup_version" integer DEFAULT 1 NOT NULL,
    "p01" double precision,
    "p02" double precision,
    "p03" double precision,
    "p04" double precision,
    "p05" double precision,
    "p06" double precision,
    "p07" double precision,
    "p08" double precision,
    "p09" double precision,
    "p10" double precision,
    "p11" double precision,
    "p12" double precision,
    "p13" double precision,
    "p14" double precision,
    "p15" double precision,
    "p16" double precision,
    "p17" double precision,
    "p18" double precision,
    "p19" double precision,
    "p20" double precision,
    "p21" double precision,
    "p22" double precision,
    "p23" double precision,
    "p24" double precision,
    "p25" double precision,
    "p26" double precision,
    "p27" double precision,
    "p28" double precision,
    "p29" double precision,
    "p30" double precision,
    "p31" double precision,
    "p32" double precision,
    "metadata" "jsonb" DEFAULT '{}'::"jsonb" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "hhs_accuracy_chk" CHECK ((("location_accuracy_m" IS NULL) OR ("location_accuracy_m" >= (0)::double precision))),
    CONSTRAINT "hhs_bucket_alignment_chk" CHECK (((EXTRACT(minute FROM ("bucket_start" AT TIME ZONE 'UTC'::"text")) = ANY (ARRAY[(0)::numeric, (30)::numeric])) AND (EXTRACT(second FROM ("bucket_start" AT TIME ZONE 'UTC'::"text")) = (0)::numeric))),
    CONSTRAINT "hhs_bucket_duration_chk" CHECK (("bucket_end" = ("bucket_start" + '00:30:00'::interval))),
    CONSTRAINT "hhs_comment_chk" CHECK ((("comment" IS NULL) OR ("char_length"("comment") <= 2000))),
    CONSTRAINT "hhs_coverage_chk" CHECK ((("coverage_seconds" >= 0) AND ("coverage_seconds" <= 1800))),
    CONSTRAINT "hhs_epoch_chk" CHECK ((("valid_epoch_count" >= 0) AND ("valid_epoch_count" <= 1800))),
    CONSTRAINT "hhs_latitude_chk" CHECK ((("latitude" IS NULL) OR (("latitude" >= ('-90'::integer)::double precision) AND ("latitude" <= (90)::double precision)))),
    CONSTRAINT "hhs_location_pair_chk" CHECK ((("latitude" IS NULL) = ("longitude" IS NULL))),
    CONSTRAINT "hhs_location_status_chk" CHECK (("location_status" = ANY (ARRAY['VALID_LIVE'::"text", 'FALLBACK_OBSERVER_BASE'::"text", 'UNAVAILABLE'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "hhs_location_validity_chk" CHECK ((("location_status" = ANY (ARRAY['UNAVAILABLE'::"text", 'LEGACY_UNKNOWN'::"text"])) OR (("latitude" IS NOT NULL) AND ("longitude" IS NOT NULL)))),
    CONSTRAINT "hhs_longitude_chk" CHECK ((("longitude" IS NULL) OR (("longitude" >= ('-180'::integer)::double precision) AND ("longitude" <= (180)::double precision)))),
    CONSTRAINT "hhs_quality_chk" CHECK (("quality_status" = ANY (ARRAY['COMPLETE'::"text", 'PARTIAL'::"text", 'NO_DATA'::"text", 'INVALID'::"text"]))),
    CONSTRAINT "hhs_range_chk" CHECK (("range_max_hz" > "range_min_hz")),
    CONSTRAINT "hhs_source_chk" CHECK (("statistics_source" = ANY (ARRAY['ROLLING'::"text", 'ANALYZER'::"text", 'OBSERVER'::"text", 'OTHER'::"text", 'LEGACY_UNKNOWN'::"text"])))
);


ALTER TABLE "public"."half_hour_statistics" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."measurement_parameter_sets" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "observer_id" "text" NOT NULL,
    "recording_id" "uuid" NOT NULL,
    "captured_at" timestamp with time zone NOT NULL,
    "latitude" double precision,
    "longitude" double precision,
    "location_accuracy_m" double precision,
    "location_timestamp" timestamp with time zone,
    "location_source" "text",
    "location_status" "text" NOT NULL,
    "comment" "text",
    "capture_origin" "text" NOT NULL,
    "trigger_type" "text" NOT NULL,
    "set_kind" "text" NOT NULL,
    "set_index" integer DEFAULT 0 NOT NULL,
    "lookup_version" integer DEFAULT 1 NOT NULL,
    "duration_s" double precision,
    "range_min_hz" double precision,
    "range_max_hz" double precision,
    "leq_dbfs" double precision,
    "lmin_dbfs" double precision,
    "lmax_dbfs" double precision,
    "l1_dbfs" double precision,
    "l10_dbfs" double precision,
    "l50_dbfs" double precision,
    "l90_dbfs" double precision,
    "l95_dbfs" double precision,
    "l99_dbfs" double precision,
    "mean_level_dbfs" double precision,
    "level_stddev_db" double precision,
    "crest_factor_db" double precision,
    "kurtosis_excess" double precision,
    "valid_epochs" integer,
    "dominant_frequency_hz" double precision,
    "p01" double precision,
    "p02" double precision,
    "p03" double precision,
    "p04" double precision,
    "p05" double precision,
    "p06" double precision,
    "p07" double precision,
    "p08" double precision,
    "p09" double precision,
    "p10" double precision,
    "p11" double precision,
    "p12" double precision,
    "p13" double precision,
    "p14" double precision,
    "p15" double precision,
    "p16" double precision,
    "p17" double precision,
    "p18" double precision,
    "p19" double precision,
    "p20" double precision,
    "p21" double precision,
    "p22" double precision,
    "p23" double precision,
    "p24" double precision,
    "p25" double precision,
    "p26" double precision,
    "p27" double precision,
    "p28" double precision,
    "p29" double precision,
    "p30" double precision,
    "p31" double precision,
    "p32" double precision,
    "metadata" "jsonb" DEFAULT '{}'::"jsonb" NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    CONSTRAINT "mps_accuracy_chk" CHECK ((("location_accuracy_m" IS NULL) OR ("location_accuracy_m" >= (0)::double precision))),
    CONSTRAINT "mps_comment_chk" CHECK ((("comment" IS NULL) OR ("char_length"("comment") <= 2000))),
    CONSTRAINT "mps_duration_chk" CHECK ((("duration_s" IS NULL) OR ("duration_s" >= (0)::double precision))),
    CONSTRAINT "mps_filter_index_chk" CHECK (((("set_kind" = 'overall_statistics'::"text") AND ("set_index" = 0)) OR (("set_kind" = 'filter_statistics'::"text") AND (("set_index" >= 1) AND ("set_index" <= 40))))),
    CONSTRAINT "mps_kind_chk" CHECK (("set_kind" = ANY (ARRAY['overall_statistics'::"text", 'filter_statistics'::"text"]))),
    CONSTRAINT "mps_latitude_chk" CHECK ((("latitude" IS NULL) OR (("latitude" >= ('-90'::integer)::double precision) AND ("latitude" <= (90)::double precision)))),
    CONSTRAINT "mps_location_pair_chk" CHECK ((("latitude" IS NULL) = ("longitude" IS NULL))),
    CONSTRAINT "mps_location_status_chk" CHECK (("location_status" = ANY (ARRAY['VALID_LIVE'::"text", 'FALLBACK_OBSERVER_BASE'::"text", 'UNAVAILABLE'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "mps_location_validity_chk" CHECK ((("location_status" = ANY (ARRAY['UNAVAILABLE'::"text", 'LEGACY_UNKNOWN'::"text"])) OR (("latitude" IS NOT NULL) AND ("longitude" IS NOT NULL)))),
    CONSTRAINT "mps_longitude_chk" CHECK ((("longitude" IS NULL) OR (("longitude" >= ('-180'::integer)::double precision) AND ("longitude" <= (180)::double precision)))),
    CONSTRAINT "mps_origin_chk" CHECK (("capture_origin" = ANY (ARRAY['OBSERVER'::"text", 'ANALYZER'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "mps_range_chk" CHECK ((("range_min_hz" IS NULL) OR ("range_max_hz" IS NULL) OR ("range_max_hz" > "range_min_hz"))),
    CONSTRAINT "mps_trigger_chk" CHECK (("trigger_type" = ANY (ARRAY['MANUAL'::"text", 'ROLLING_LEVEL'::"text", 'ANALYZER_LEVEL'::"text", 'HARDWARE_EXTERNAL'::"text", 'LEGACY_UNKNOWN'::"text"]))),
    CONSTRAINT "mps_valid_epochs_chk" CHECK ((("valid_epochs" IS NULL) OR ("valid_epochs" >= 0)))
);


ALTER TABLE "public"."measurement_parameter_sets" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."observations" (
    "id" bigint NOT NULL,
    "event_id" "uuid" NOT NULL,
    "observer_id" "text" NOT NULL,
    "scheduled_time" timestamp with time zone NOT NULL,
    "response_time" timestamp with time zone,
    "rating" smallint NOT NULL,
    "latitude" double precision DEFAULT 0 NOT NULL,
    "longitude" double precision DEFAULT 0 NOT NULL,
    "app_version" "text",
    "wav_present" boolean DEFAULT false NOT NULL,
    "wav_path" "text",
    "wav_duration_sec" integer,
    "wav_sample_rate" integer,
    "wav_channels" integer,
    "wav_bits_per_sample" integer,
    "wav_size_bytes" bigint,
    "wav_sha256" "text",
    "received_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "comment" "text",
    CONSTRAINT "observations_latitude_chk" CHECK ((("latitude" >= ('-90'::integer)::double precision) AND ("latitude" <= (90)::double precision))),
    CONSTRAINT "observations_longitude_chk" CHECK ((("longitude" >= ('-180'::integer)::double precision) AND ("longitude" <= (180)::double precision))),
    CONSTRAINT "observations_rating_chk" CHECK ((("rating" >= '-1'::integer) AND ("rating" <= 5))),
    CONSTRAINT "observations_wav_bits_chk" CHECK ((("wav_bits_per_sample" IS NULL) OR ("wav_bits_per_sample" = ANY (ARRAY[8, 16, 24, 32])))),
    CONSTRAINT "observations_wav_channels_chk" CHECK ((("wav_channels" IS NULL) OR (("wav_channels" >= 1) AND ("wav_channels" <= 8)))),
    CONSTRAINT "observations_wav_duration_chk" CHECK ((("wav_duration_sec" IS NULL) OR (("wav_duration_sec" >= 1) AND ("wav_duration_sec" <= 600)))),
    CONSTRAINT "observations_wav_sample_rate_chk" CHECK ((("wav_sample_rate" IS NULL) OR (("wav_sample_rate" >= 100) AND ("wav_sample_rate" <= 192000)))),
    CONSTRAINT "observations_wav_sha256_chk" CHECK ((("wav_sha256" IS NULL) OR ("wav_sha256" ~ '^[0-9a-f]{64}$'::"text")))
);


ALTER TABLE "public"."observations" OWNER TO "postgres";


COMMENT ON COLUMN "public"."observations"."comment" IS 'Optional free-text comment entered by the observer for ratings 0..5; null for no comment and automatic rating -1.';



ALTER TABLE "public"."observations" ALTER COLUMN "id" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."observations_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE OR REPLACE VIEW "public"."observations_with_location_status" AS
 SELECT "id",
    "event_id",
    "observer_id",
    "scheduled_time",
    "response_time",
    "rating",
    "latitude",
    "longitude",
    "app_version",
    "wav_present",
    "wav_path",
    "wav_duration_sec",
    "wav_sample_rate",
    "wav_channels",
    "wav_bits_per_sample",
    "wav_size_bytes",
    "wav_sha256",
    "received_at",
    (NOT (("latitude" = (0)::double precision) AND ("longitude" = (0)::double precision))) AS "location_available"
   FROM "public"."observations" "o";


ALTER VIEW "public"."observations_with_location_status" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."observers" (
    "observer_id" "text" NOT NULL,
    "latitude" double precision DEFAULT 0 NOT NULL,
    "longitude" double precision DEFAULT 0 NOT NULL,
    "key_hash" "text" NOT NULL,
    "enabled" boolean DEFAULT true NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "status" "text" DEFAULT 'active'::"text" NOT NULL,
    "revoked_at" timestamp with time zone,
    "paused_at" timestamp with time zone,
    CONSTRAINT "observers_key_hash_chk" CHECK (("key_hash" ~ '^[0-9a-f]{64}$'::"text")),
    CONSTRAINT "observers_latitude_chk" CHECK ((("latitude" >= ('-90'::integer)::double precision) AND ("latitude" <= (90)::double precision))),
    CONSTRAINT "observers_longitude_chk" CHECK ((("longitude" >= ('-180'::integer)::double precision) AND ("longitude" <= (180)::double precision))),
    CONSTRAINT "observers_status_check" CHECK (("status" = ANY (ARRAY['active'::"text", 'paused'::"text", 'revoked'::"text"])))
);


ALTER TABLE "public"."observers" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."parameter_lookup" (
    "lookup_version" integer NOT NULL,
    "set_kind" "text" NOT NULL,
    "slot_no" integer NOT NULL,
    "slot_code" "text" NOT NULL,
    "semantic_key" "text",
    "display_label" "text",
    "unit" "text",
    "value_type" "text" DEFAULT 'double'::"text" NOT NULL,
    "active" boolean DEFAULT false NOT NULL,
    "description" "text",
    "introduced_in" "text",
    "notes" "text",
    CONSTRAINT "parameter_lookup_slot_no_check" CHECK ((("slot_no" >= 1) AND ("slot_no" <= 32)))
);


ALTER TABLE "public"."parameter_lookup" OWNER TO "postgres";


ALTER TABLE ONLY "public"."app_config"
    ADD CONSTRAINT "app_config_pkey" PRIMARY KEY ("config_id");



ALTER TABLE ONLY "public"."audio_recordings"
    ADD CONSTRAINT "audio_recordings_pkey" PRIMARY KEY ("recording_id");



ALTER TABLE ONLY "public"."half_hour_statistics"
    ADD CONSTRAINT "half_hour_statistics_observer_id_bucket_start_statistics_so_key" UNIQUE ("observer_id", "bucket_start", "statistics_source", "range_min_hz", "range_max_hz");



ALTER TABLE ONLY "public"."half_hour_statistics"
    ADD CONSTRAINT "half_hour_statistics_pkey" PRIMARY KEY ("bucket_id");



ALTER TABLE ONLY "public"."measurement_parameter_sets"
    ADD CONSTRAINT "measurement_parameter_sets_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."measurement_parameter_sets"
    ADD CONSTRAINT "measurement_parameter_sets_recording_id_set_kind_set_index__key" UNIQUE ("recording_id", "set_kind", "set_index", "lookup_version");



ALTER TABLE ONLY "public"."observations"
    ADD CONSTRAINT "observations_event_id_key" UNIQUE ("event_id");



ALTER TABLE ONLY "public"."observations"
    ADD CONSTRAINT "observations_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."observers"
    ADD CONSTRAINT "observers_key_hash_key" UNIQUE ("key_hash");



ALTER TABLE ONLY "public"."observers"
    ADD CONSTRAINT "observers_pkey" PRIMARY KEY ("observer_id");



ALTER TABLE ONLY "public"."parameter_lookup"
    ADD CONSTRAINT "parameter_lookup_lookup_version_set_kind_slot_code_key" UNIQUE ("lookup_version", "set_kind", "slot_code");



ALTER TABLE ONLY "public"."parameter_lookup"
    ADD CONSTRAINT "parameter_lookup_pkey" PRIMARY KEY ("lookup_version", "set_kind", "slot_no");



CREATE INDEX "idx_hhs_observer_time" ON "public"."half_hour_statistics" USING "btree" ("observer_id", "bucket_start");



CREATE INDEX "idx_hhs_time" ON "public"."half_hour_statistics" USING "btree" ("bucket_start");



CREATE INDEX "idx_mps_observer_time" ON "public"."measurement_parameter_sets" USING "btree" ("observer_id", "captured_at");



CREATE INDEX "idx_mps_recording" ON "public"."measurement_parameter_sets" USING "btree" ("recording_id");



CREATE INDEX "observations_observer_time_idx" ON "public"."observations" USING "btree" ("observer_id", "scheduled_time");



CREATE INDEX "observations_scheduled_time_idx" ON "public"."observations" USING "btree" ("scheduled_time");



ALTER TABLE ONLY "public"."audio_recordings"
    ADD CONSTRAINT "audio_recordings_observer_id_fkey" FOREIGN KEY ("observer_id") REFERENCES "public"."observers"("observer_id");



ALTER TABLE ONLY "public"."half_hour_statistics"
    ADD CONSTRAINT "half_hour_statistics_observer_id_fkey" FOREIGN KEY ("observer_id") REFERENCES "public"."observers"("observer_id");



ALTER TABLE ONLY "public"."measurement_parameter_sets"
    ADD CONSTRAINT "measurement_parameter_sets_observer_id_fkey" FOREIGN KEY ("observer_id") REFERENCES "public"."observers"("observer_id");



ALTER TABLE ONLY "public"."measurement_parameter_sets"
    ADD CONSTRAINT "measurement_parameter_sets_recording_id_fkey" FOREIGN KEY ("recording_id") REFERENCES "public"."audio_recordings"("recording_id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."observations"
    ADD CONSTRAINT "observations_observer_id_fkey" FOREIGN KEY ("observer_id") REFERENCES "public"."observers"("observer_id");



ALTER TABLE "public"."app_config" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."audio_recordings" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."half_hour_statistics" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."measurement_parameter_sets" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."observations" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."observers" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."parameter_lookup" ENABLE ROW LEVEL SECURITY;




ALTER PUBLICATION "supabase_realtime" OWNER TO "postgres";


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";





































































































































































GRANT ALL ON TABLE "public"."app_config" TO "service_role";



GRANT ALL ON TABLE "public"."audio_recordings" TO "anon";
GRANT ALL ON TABLE "public"."audio_recordings" TO "authenticated";
GRANT ALL ON TABLE "public"."audio_recordings" TO "service_role";



GRANT ALL ON TABLE "public"."half_hour_statistics" TO "anon";
GRANT ALL ON TABLE "public"."half_hour_statistics" TO "authenticated";
GRANT ALL ON TABLE "public"."half_hour_statistics" TO "service_role";



GRANT ALL ON TABLE "public"."measurement_parameter_sets" TO "anon";
GRANT ALL ON TABLE "public"."measurement_parameter_sets" TO "authenticated";
GRANT ALL ON TABLE "public"."measurement_parameter_sets" TO "service_role";



GRANT ALL ON TABLE "public"."observations" TO "service_role";



GRANT ALL ON SEQUENCE "public"."observations_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."observations_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."observations_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."observations_with_location_status" TO "service_role";



GRANT ALL ON TABLE "public"."observers" TO "service_role";



GRANT ALL ON TABLE "public"."parameter_lookup" TO "anon";
GRANT ALL ON TABLE "public"."parameter_lookup" TO "authenticated";
GRANT ALL ON TABLE "public"."parameter_lookup" TO "service_role";









ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";































