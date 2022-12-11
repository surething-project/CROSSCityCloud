-- Using Intermediate Result Tables (Practical Implementation)

-- Stable Set Epoch Table Creation Query (Example for Gulbenkian 2019-07-29 : 2019-08-04)
WITH bssid_count AS (
  SELECT bssid, SUM(count) as total_count FROM (SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-07-29` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-07-30` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-07-31` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-08-01` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-08-02` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-08-03` UNION ALL SELECT poi_id, bssid, count FROM `gsdsupport.cross_wifi_ap_obs.stable_gulbenkian_2019-08-04`) WHERE poi_id = 'gulbenkian' GROUP BY bssid HAVING total_count >= 1
)

SELECT bssid, total_count FROM bssid_count WHERE total_count >= (SELECT PERCENTILE_CONT(total_count, 0.9) OVER() as percentile_count FROM (SELECT * FROM bssid_count) LIMIT 1)

-- Query Stable Set Epoch Table (Example Gulbenkian Epoch 2019-07-29 : 2019-08-04)
SELECT bssid FROM `gsdsupport.cross_wifi_ap_obs.stable_set_gulbenkian_2019-07-29`

-- Volatile Set (Example Gulbenkian from 2019-07-30 14:15:03 to 2019-07-30 14:35:03)
WITH bssid_count AS (
  SELECT bssid, SUM(count) as total_count FROM `gsdsupport.cross_wifi_ap_obs.volatile_gulbenkian_2019-07-30` WHERE poi_id = 'gulbenkian' AND bssid NOT IN (SELECT bssid from `gsdsupport.cross_wifi_ap_obs.stable_set_gulbenkian_2019-07-29`) AND start_time >= TIMESTAMP("2019-07-30 14:15:03") AND end_time <= TIMESTAMP("2019-07-30 14:35:03") GROUP BY bssid HAVING total_count >= 1
)

SELECT bssid FROM bssid_count WHERE total_count <= (SELECT PERCENTILE_CONT(total_count, 0.1) OVER() as percentile_count FROM (SELECT * FROM bssid_count) LIMIT 1)


-- Using a Single Observation Table (Theoretical Implementation)

-- Query Stable Set Epoch Table (Example Gulbenkian Epoch 2019-07-29 : 2019-08-04)
WITH bssid_count AS (
  SELECT transmitter_id, COUNT(transmitter_id) as total_count FROM `gsdsupport.cross_claro.obs` WHERE poi_id = 'gulbenkian' AND obs_time >= "2019-07-29" AND obs_time < "2019-08-05" GROUP BY transmitter_id HAVING total_count >= 1
)

SELECT transmitter_id, total_count FROM bssid_count WHERE total_count >= (SELECT PERCENTILE_CONT(total_count, 0.9) OVER() as percentile_count FROM (SELECT * FROM bssid_count) LIMIT 1)

-- Volatile Set (Example Gulbenkian from 2019-07-30 14:15:03 to 2019-07-30 14:35:03)
WITH stable_bssid_count AS (
  SELECT transmitter_id, COUNT(transmitter_id) as total_count FROM `gsdsupport.cross_claro.obs` WHERE poi_id = 'gulbenkian' AND obs_time >= "2019-07-29" AND obs_time < "2019-08-05" GROUP BY transmitter_id HAVING total_count >= 1
),
stable_set AS (
  SELECT transmitter_id FROM stable_bssid_count WHERE total_count >= (SELECT PERCENTILE_CONT(total_count, 0.9) OVER() as percentile_count FROM (SELECT * FROM stable_bssid_count) LIMIT 1)
),
volatile_bssid_count AS (
  SELECT transmitter_id, COUNT(transmitter_id) as total_count FROM `gsdsupport.cross_claro.obs` WHERE poi_id = 'gulbenkian' AND transmitter_id NOT IN (SELECT transmitter_id FROM stable_set) AND obs_time >= TIMESTAMP("2019-07-30 14:15:03") AND obs_time <= TIMESTAMP("2019-07-30 14:35:03") GROUP BY transmitter_id HAVING total_count >= 1
)

SELECT transmitter_id FROM volatile_bssid_count WHERE total_count <= (SELECT PERCENTILE_CONT(total_count, 0.1) OVER() as percentile_count FROM (SELECT * FROM volatile_bssid_count) LIMIT 1)