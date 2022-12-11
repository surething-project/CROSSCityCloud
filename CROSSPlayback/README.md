<p align="center">
  <img src="../assets/CROSS-Logo.png" width="200" height="200" alt="CROSS Logo"/>
</p>

<h3 align="center">CROSSPlayback</h3>
<h4 align="center"><i>Java Client for Replaying Historic WiFi AP Observations</i></h4>

---

## Table of Contents

- [Build and Replay Observations](#build-and-replay-observations)
    - [Prerequisites](#prerequisites)
    - [Setup WiFi AP Observation Files](#setup-wifi-ap-observation-files)
    - [Resolve the Dependencies and Build the Client](#resolve-the-dependencies-and-build-the-client)
    - [Run the Client](#run-the-client)
- [Benchmark Environment](#benchmark-environment)
    - [Build and Push Docker Image](#build-and-push-docker-image)
    - [Run Pod in GKE Cluster](#run-pod-in-gke-cluster)    
- [Authors](#authors)

## Build and Replay Observations

### Prerequisites

- Java Development Kit (JDK) >= 11
- Maven >= 3.8
- Build the [CROSSContract](https://github.com/inesc-id/SureThing_Wireless_Data/tree/main/CROSS-Contract)

### Setup WiFi AP Observation Files

CROSSPlayback expects observation files to be labeled with the user ID and placed under a common directory with a timestamp and POI path. As an example, the file containing the observations collected by the user with ID "alice", in the POI "Alvalade", during the day "2020-01-19" has the following path:

```shell script
~/cross-field-experiments/2020-01-19/Alvalade/alice.txt
```

Observations within the file must be formatted as follows:

```shell script
1579452793,""
1579452762047,(38.737206500, -9.153219516),-123.03732308516089,16.0
"FCG Free WiFi",f4:cf:e2:fb:97:5f,[ESS],5180 MHz,-53 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"FCG Eventos 5Ghz",f4:cf:e2:fb:97:59,[WPA2-PSK-CCMP][ESS],5180 MHz,-53 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"FCG",f4:cf:e2:fb:97:58,[WPA2-PSK-CCMP][ESS],5180 MHz,-53 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"",f4:cf:e2:fb:97:5a,[WPA2-PSK-CCMP][ESS],5180 MHz,-54 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"",f4:cf:e2:fb:97:55,[WPA2-PSK-CCMP][ESS],2462 MHz,-54 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"",f4:cf:e2:fb:97:5e,[WPA2-EAP-CCMP][ESS],5180 MHz,-54 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"FCG Free WiFi",f4:cf:e2:fb:97:50,[ESS],2462 MHz,-55 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"",f4:cf:e2:fb:97:54,[WPA2-PSK-CCMP][ESS],2462 MHz,-56 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"FCG",f4:cf:e2:fb:97:57,[WPA2-PSK-CCMP][ESS],2462 MHz,-56 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"FCG Eventos",f4:cf:e2:fb:97:53,[WPA2-PSK-CCMP][ESS],2462 MHz,-56 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
"",f4:cf:e2:fb:97:51,[WPA2-EAP-CCMP][ESS],2462 MHz,-57 dBm,CenterFreq0 0 MHz,CenterFreq1 0 MHz,ChannelWidth 0 MHz
```

### Resolve the Dependencies and Build the Client

From the root of the project execute:

```shell script
mvn clean install
```

### Run the Client

CROSSPlayback expects to receive as arguments the path for the common directory, the timestamp, the POI, the User ID, the threshold in seconds for a visit duration and the mode of interaction (SCAVENGER -> to submit visits with evidences to feed the scavenger pipeline | VOLATILE -> to submit visits with evidences to test the level of volatility of the visit)

Here is an example:

```shell script
mvn exec:java -Dexec.args="FILE ../../../Tese/cross-field-experiments 2020-01-19 Gulbenkian alice 450 VOLATILE"
```

## Benchmark Environment

### Build and Push Docker Image

Execute the build script on the project root:

```shell script
./buildDockerImage.sh
```

Tag with version and push to container registry: 

```shell script
docker tag cross-playback gcr.io/gsdsupport/cross-playback:v1.0.0
```

```shell script
gcloud docker -- push gcr.io/gsdsupport/cross-playback:v1.0.0
```

### Run Pod in GKE Cluster

Create the GKE cluster:

```shell script
gcloud container clusters create "cross-playback-gke" \
  --region "europe-west1" \
  --machine-type "e2-standard-2" \
  --num-nodes "1" --node-locations "europe-west1-b","europe-west1-c"
```

Get the GKE cluster credentials:
```shell script
gcloud container clusters get-credentials cross-playback-gke --region europe-west1
```

Run desired number of pods with desired experiment arguments. Execution examples:

```shell script
kubectl run playback --image=gcr.io/gsdsupport/cross-playback:v1.0.2 -- mvn exec:java -Dexec.args="SIMULATION comercio 297"
```

```shell script
kubectl run playback --image=gcr.io/gsdsupport/cross-playback:v1.0.2 -- mvn exec:java -Dexec.args="FILE ./cross-field-experiments 2019-08-04 Gulbenkian alice 450 VOLATILE"
```

Clean the environment (delete pod and cluster):

```shell script
kubectl delete pod playback
```

```shell script
gcloud container clusters delete "cross-playback-gke" \
  --region "europe-west1"
```

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lucas Vicente     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:lucasdhvicente@gmail.com "lucasdhvicente@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/WARSKELETON "WARSKELETON") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/lucas-vicente-a91819184/ "lucas-vicente-a91819184") |
