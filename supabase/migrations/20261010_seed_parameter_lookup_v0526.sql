-- LFN-Scout v0.5.26
-- Additive seed migration for parameter_lookup
-- Date: 2026-10-10
-- Purpose: populate lookup_version 1 mappings without altering existing schema.
-- Safe to re-run because of ON CONFLICT.

begin;

insert into public.parameter_lookup
(
    lookup_version, set_kind, slot_no, slot_code, semantic_key,
    display_label, unit, value_type, active, description, introduced_in, notes
)
values
(1,'overall_statistics',1,'P01','leq_dbfs','Leq','dBFS','double',true,'Equivalent unweighted level over configured statistics band/time.','v0.5.26',null),
(1,'overall_statistics',2,'P02','min_epoch_dbfs','Min','dBFS','double',true,'Minimum epoch level.','v0.5.26',null),
(1,'overall_statistics',3,'P03','max_epoch_dbfs','Max','dBFS','double',true,'Maximum epoch level.','v0.5.26',null),
(1,'overall_statistics',4,'P04','l1_dbfs','L1','dBFS','double',true,'L1 exceedance level.','v0.5.26',null),
(1,'overall_statistics',5,'P05','l10_dbfs','L10','dBFS','double',true,'L10 exceedance level.','v0.5.26',null),
(1,'overall_statistics',6,'P06','l50_dbfs','L50','dBFS','double',true,'L50 exceedance level.','v0.5.26',null),
(1,'overall_statistics',7,'P07','l90_dbfs','L90','dBFS','double',true,'L90 exceedance level.','v0.5.26',null),
(1,'overall_statistics',8,'P08','l95_dbfs','L95','dBFS','double',true,'L95 exceedance level.','v0.5.26',null),
(1,'overall_statistics',9,'P09','l99_dbfs','L99','dBFS','double',true,'L99 exceedance level.','v0.5.26',null),
(1,'overall_statistics',10,'P10','mean_epoch_dbfs','Mean','dBFS','double',true,'Mean epoch level.','v0.5.26',null),
(1,'overall_statistics',11,'P11','stddev_epoch_db','StdDev','dB','double',true,'Epoch-level standard deviation.','v0.5.26',null),
(1,'overall_statistics',12,'P12','crest_db','Crest','dB','double',true,'Peak/RMS crest factor.','v0.5.26',null),
(1,'overall_statistics',13,'P13','excess_kurtosis','Excess kurtosis','1','double',true,'Excess kurtosis.','v0.5.26',null),
(1,'overall_statistics',14,'P14','epoch_count','Epoch count','count','integer',true,'Number of evaluated epochs.','v0.5.26',null),
(1,'overall_statistics',15,'P15','dominant_frequency_hz','Dominant frequency','Hz','double',true,'Dominant frequency in configured analysis band.','v0.5.26',null),

(1,'filter_statistics',1,'P01','leq_dbfs','Leq','dBFS','double',true,'Equivalent level in this filter band.','v0.5.26',null),
(1,'filter_statistics',2,'P02','min_epoch_dbfs','Min','dBFS','double',true,'Minimum epoch level in this filter band.','v0.5.26',null),
(1,'filter_statistics',3,'P03','max_epoch_dbfs','Max','dBFS','double',true,'Maximum epoch level in this filter band.','v0.5.26',null),
(1,'filter_statistics',4,'P04','l1_dbfs','L1','dBFS','double',true,'L1 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',5,'P05','l10_dbfs','L10','dBFS','double',true,'L10 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',6,'P06','l50_dbfs','L50','dBFS','double',true,'L50 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',7,'P07','l90_dbfs','L90','dBFS','double',true,'L90 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',8,'P08','l95_dbfs','L95','dBFS','double',true,'L95 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',9,'P09','l99_dbfs','L99','dBFS','double',true,'L99 exceedance level in this filter band.','v0.5.26',null),
(1,'filter_statistics',10,'P10','mean_epoch_dbfs','Mean','dBFS','double',true,'Mean epoch level in this filter band.','v0.5.26',null),
(1,'filter_statistics',11,'P11','stddev_epoch_db','StdDev','dB','double',true,'Epoch-level standard deviation in filter band.','v0.5.26',null),
(1,'filter_statistics',12,'P12','crest_db','Crest','dB','double',true,'Peak/RMS crest factor in this filter band.','v0.5.26',null),
(1,'filter_statistics',13,'P13','excess_kurtosis','Excess kurtosis','1','double',true,'Excess kurtosis in this filter band.','v0.5.26',null),
(1,'filter_statistics',14,'P14','epoch_count','Epoch count','count','integer',true,'Number of evaluated epochs in this filter band.','v0.5.26',null);

-- Add reserved slots P16..P32 for overall and P15..P32 for filter.
insert into public.parameter_lookup
(lookup_version, set_kind, slot_no, slot_code, semantic_key, display_label, unit, value_type, active, description, introduced_in, notes)
select 1, 'overall_statistics', n, 'P' || lpad(n::text,2,'0'), null, null, null, 'double', false, 'Reserved.', 'v0.5.26', null
from generate_series(16,32) n
on conflict (lookup_version, set_kind, slot_no) do nothing;

insert into public.parameter_lookup
(lookup_version, set_kind, slot_no, slot_code, semantic_key, display_label, unit, value_type, active, description, introduced_in, notes)
select 1, 'filter_statistics', n, 'P' || lpad(n::text,2,'0'), null, null, null, 'double', false, 'Reserved.', 'v0.5.26', null
from generate_series(15,32) n
on conflict (lookup_version, set_kind, slot_no) do nothing;

commit;
