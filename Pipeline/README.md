<p align="center">
  <img src="../assets/beam-logo.png" width="200" height="200" alt="Apache Beam Logo"/>
</p>

<h3 align="center">CROSS Scavenger WiFi AP Observations Pipeline</h3>
<h4 align="center"><i>Apache Beam Stream Pipeline for Real Time and Historic Processing</i></h4>

---

## Table of Contents

- [Build and Run](#build-and-run)
    - [Prerequisites](#prerequisites)
    - [Execute the Pipeline Locally](#execute-the-pipeline-locally)
    - [Execute the Pipeline deployed to Google Cloud Dataflow](#execute-the-pipeline-deployed-to-google-cloud-dataflow)
    - [Extra Pipeline Options](#extra-pipeline-options)
    - [Execute Rui Claro Base Solution Loader](#execute-rui-claro-base-solution-loader)
- [Authors](#authors)

## Build and Run

### Prerequisites

- Java Development Kit (JDK) >= 11
- Maven >= 3.8
- Build the [CROSSContract](https://github.com/inesc-id/SureThing_Wireless_Data/tree/main/CROSS-Contract)
- Topic in Cloud Pub/Sub
- Cloud BigQuery Dataset
- Cloud Storage Bucket (for Google Cloud Dataflow)

### Execute the Pipeline Locally

For historical data processing:

```shell script
mvn -Pdirect-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.WifiRunner -Dexec.args="--isRealTime=false --project=gsdsupport --bigQueryCrossDataset=cross_wifi_ap_obs --bigQueryStableSetTablePrefix=stable_ --bigQueryVolatileSetTablePrefix=volatile_ --pubSubTopic=wifi-ap-obs-topic --runner=DirectRunner"
```

For real time data processing:

```shell script
mvn -Pdirect-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.WifiRunner -Dexec.args="--isRealTime=true --project=gsdsupport --bigQueryCrossDataset=cross_wifi_ap_obs --bigQueryStableSetTablePrefix=stable_ --bigQueryVolatileSetTablePrefix=volatile_ --pubSubTopic=wifi-ap-obs-topic --runner=DirectRunner"
```

### Execute the Pipeline Deployed to Google Cloud Dataflow

For historical data processing:

```shell script
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.WifiRunner -Dexec.args="--isRealTime=false --project=gsdsupport --bigQueryCrossDataset=cross_wifi_ap_obs --bigQueryStableSetTablePrefix=stable_ --bigQueryVolatileSetTablePrefix=volatile_ --pubSubTopic=wifi-ap-obs-topic --gcpTempLocation=gs://cross-scavenger-pipeline/temp/ --region=europe-west1 --runner=DataflowRunner"
```

For real time data processing:

```shell script
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.WifiRunner -Dexec.args="--isRealTime=true --project=gsdsupport --bigQueryCrossDataset=cross_wifi_ap_obs --bigQueryStableSetTablePrefix=stable_ --bigQueryVolatileSetTablePrefix=volatile_ --pubSubTopic=wifi-ap-obs-topic --gcpTempLocation=gs://cross-scavenger-pipeline/temp/ --region=europe-west1 --runner=DataflowRunner"
```

### Extra Pipeline Options

Stable set intermediate result window seconds size (default value is set to 24 hours _equal to a period_):

```shell script
--stableSetWindowSeconds=86400
```

Volatile set intermediate result window seconds size (default value is set to 5 minutes _equal to the delta greatest common divisor_):

```shell script
--volatileSetWindowSeconds=300
```

### Execute Rui Claro Base Solution Loader

Locally:

```shell script
mvn -Pdirect-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.ClaroRunner -Dexec.args="--project=gsdsupport --bigQueryCrossDataset=cross_claro --bigQueryObsTablePrefix=obs --pubSubTopic=wifi-ap-obs-topic --runner=DirectRunner"
```

Deployed to Dataflow:

```shell script
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=pt.ulisboa.tecnico.cross.ClaroRunner -Dexec.args="--project=gsdsupport --bigQueryCrossDataset=cross_claro --bigQueryObsTablePrefix=obs --pubSubTopic=wifi-ap-obs-topic --gcpTempLocation=gs://cross-scavenger-pipeline/temp/ --region=europe-west1 --runner=DataflowRunner"
```

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lucas Vicente     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:lucasdhvicente@gmail.com "lucasdhvicente@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/WARSKELETON "WARSKELETON") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/lucas-vicente-a91819184/ "lucas-vicente-a91819184") |
