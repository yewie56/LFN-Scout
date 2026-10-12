import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};
const metricNames = [
  "leq_dbfs","lmin_dbfs","lmax_dbfs","l1_dbfs","l10_dbfs","l50_dbfs","l90_dbfs",
  "l95_dbfs","l99_dbfs","mean_level_dbfs","level_stddev_db","crest_factor_db",
  "kurtosis_excess","valid_epochs","dominant_frequency_hz",
] as const;
function response(body: unknown, status=200) { return new Response(JSON.stringify(body), {status, headers:{...corsHeaders,"Content-Type":"application/json"}}); }
async function sha256Hex(value:string){const d=await crypto.subtle.digest("SHA-256",new TextEncoder().encode(value));return Array.from(new Uint8Array(d)).map(b=>b.toString(16).padStart(2,"0")).join("");}
function serviceRoleKey(){const direct=Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");const packed=Deno.env.get("SUPABASE_SECRET_KEYS");if(packed){try{const p=JSON.parse(packed);if(p&&typeof p.default==="string"&&p.default.trim())return p.default.trim();}catch{if(packed.trim())return packed.trim();}}return direct?.trim()||null;}
function slotColumns(parameters:unknown){const out:Record<string,number|null>={};if(parameters==null)return out;if(typeof parameters!=="object"||Array.isArray(parameters))return null;for(const [k,v] of Object.entries(parameters as Record<string,unknown>)){const m=/^P(0[1-9]|[12][0-9]|3[0-2])$/.exec(k.toUpperCase());if(!m)return null;const n=Number(v);if(v!==null&&!Number.isFinite(n))return null;out[`p${m[1]}`]=v===null?null:n;}return out;}

Deno.serve(async(req:Request)=>{
  if(req.method==="OPTIONS")return new Response("ok",{headers:corsHeaders});
  if(req.method==="GET")return response({ok:true,service:"submit_audio_statistics",version:"1.0.0"});
  if(req.method!=="POST")return response({error:"method_not_allowed"},405);
  try{
    const b=await req.json();
    const key=typeof b?.key==="string"?b.key.trim():"";
    const recordingId=typeof b?.recording_id==="string"?b.recording_id.trim():"";
    const setKind=typeof b?.set_kind==="string"?b.set_kind.trim():"";
    const setIndex=Number(b?.set_index);
    const lookupVersion=Number(b?.lookup_version??1);
    const duration= b?.duration_s==null?null:Number(b.duration_s);
    const rangeMin=b?.range_min_hz==null?null:Number(b.range_min_hz);
    const rangeMax=b?.range_max_hz==null?null:Number(b.range_max_hz);
    const metrics=b?.metrics==null?{}:b.metrics;
    const parameters=slotColumns(b?.parameters);
    if(!key||!recordingId||!["overall_statistics","filter_statistics"].includes(setKind)||!Number.isInteger(setIndex)||
       (setKind==="overall_statistics"&&setIndex!==0)||(setKind==="filter_statistics"&&(setIndex<1||setIndex>40))||
       !Number.isInteger(lookupVersion)||lookupVersion<1||(duration!=null&&(!Number.isFinite(duration)||duration<0))||
       (rangeMin!=null&&!Number.isFinite(rangeMin))||(rangeMax!=null&&!Number.isFinite(rangeMax))||
       (rangeMin!=null&&rangeMax!=null&&rangeMax<=rangeMin)||typeof metrics!=="object"||Array.isArray(metrics)||parameters==null)
       return response({error:"invalid_statistics_request"},400);
    const metricValues:Record<string,number|null>={};
    for(const [k,v] of Object.entries(metrics as Record<string,unknown>)){
      if(!(metricNames as readonly string[]).includes(k))return response({error:"unknown_metric",metric:k},400);
      if(v===null){metricValues[k]=null;continue;} const n=Number(v); if(!Number.isFinite(n))return response({error:"invalid_metric",metric:k},400);
      if(k==="valid_epochs"&&!Number.isInteger(n))return response({error:"invalid_metric",metric:k},400); metricValues[k]=n;
    }
    const url=Deno.env.get("SUPABASE_URL"),service=serviceRoleKey();if(!url||!service)return response({error:"server_configuration_error"},500);
    const sb=createClient(url,service,{auth:{persistSession:false,autoRefreshToken:false}});
    const hash=await sha256Hex(key);
    const {data:observer,error:oe}=await sb.from("observers").select("observer_id,enabled,status").eq("key_hash",hash).maybeSingle();
    if(oe)return response({error:"observer_lookup_failed"},500);if(!observer||observer.enabled!==true)return response({error:"invalid_or_disabled_key"},401);if(observer.status==="revoked")return response({error:"key_revoked"},403);if(observer.status==="paused")return response({error:"key_paused"},403);
    const {data:audio,error:ae}=await sb.from("audio_recordings").select("recording_id,observer_id,recorded_at,latitude,longitude,location_accuracy_m,location_timestamp,location_source,location_status,comment,capture_origin,trigger_type").eq("recording_id",recordingId).maybeSingle();
    if(ae)return response({error:"audio_lookup_failed"},500);if(!audio)return response({error:"recording_not_found"},404);if(audio.observer_id!==observer.observer_id)return response({error:"recording_not_owned"},403);
    const row:any={observer_id:observer.observer_id,recording_id:recordingId,captured_at:audio.recorded_at,latitude:audio.latitude,longitude:audio.longitude,location_accuracy_m:audio.location_accuracy_m,location_timestamp:audio.location_timestamp,location_source:audio.location_source,location_status:audio.location_status,comment:audio.comment,capture_origin:audio.capture_origin,trigger_type:audio.trigger_type,set_kind:setKind,set_index:setIndex,lookup_version:lookupVersion,duration_s:duration,range_min_hz:rangeMin,range_max_hz:rangeMax,...metricValues,...parameters,metadata:(typeof b?.metadata==="object"&&b.metadata&&!Array.isArray(b.metadata))?b.metadata:{}};
    const {data:existing,error:ee}=await sb.from("measurement_parameter_sets").select("id").eq("recording_id",recordingId).eq("set_kind",setKind).eq("set_index",setIndex).eq("lookup_version",lookupVersion).maybeSingle();
    if(ee)return response({error:"duplicate_lookup_failed"},500);
    if(existing){const {error}=await sb.from("measurement_parameter_sets").update(row).eq("id",existing.id);if(error)return response({error:"statistics_update_failed"},500);return response({ok:true,duplicate:true,id:existing.id});}
    const {data:created,error:ie}=await sb.from("measurement_parameter_sets").insert(row).select("id").single();if(ie)return response({error:"statistics_insert_failed"},500);return response({ok:true,duplicate:false,id:created.id});
  }catch(e){console.error(e);return response({error:"invalid_request"},400);}
});
