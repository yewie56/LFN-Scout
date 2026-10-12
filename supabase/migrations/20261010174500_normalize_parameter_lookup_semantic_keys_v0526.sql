-- LFN-Scout v0.5.26
-- Additive Korrekturmigration:
-- Vereinheitlicht parameter_lookup.semantic_key mit den kanonischen
-- Feldnamen von measurement_parameter_sets / submit_audio_statistics.
--
-- Bereits angewendete Migrationen werden NICHT verändert.
-- Gilt für lookup_version = 1 und beide set_kind-Werte.

begin;

update public.parameter_lookup
set semantic_key = case semantic_key
    when 'min_epoch_dbfs'      then 'lmin_dbfs'
    when 'max_epoch_dbfs'      then 'lmax_dbfs'
    when 'mean_epoch_dbfs'     then 'mean_level_dbfs'
    when 'stddev_epoch_db'     then 'level_stddev_db'
    when 'crest_db'            then 'crest_factor_db'
    when 'excess_kurtosis'     then 'kurtosis_excess'
    when 'epoch_count'         then 'valid_epochs'
    else semantic_key
end
where lookup_version = 1
  and semantic_key in (
      'min_epoch_dbfs',
      'max_epoch_dbfs',
      'mean_epoch_dbfs',
      'stddev_epoch_db',
      'crest_db',
      'excess_kurtosis',
      'epoch_count'
  );

commit;
