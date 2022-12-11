<p align="center">
  <img src="../assets/CROSS-Logo.png" width="200" height="200" alt="CROSS Logo"/>
</p>

<h3 align="center">CROSS City Server V2</h3>
<h4 align="center">Extension of <a href="https://github.com/inesc-id/CROSS-server">CROSS City V1</a>
developed by <a href="https://github.com/gbl08ma">@gbl08ma</a></h4>
<h4 align="center"><i>REST API and PSQL DB</i></h4>

---

<p align="center"><i>loCation pROof techniqueS for consumer mobile applicationS</i></p>

## Table of Contents

- [Build and Run From Source](#build-and-run-from-source)
    - [Source Prerequisites](#source-prerequisites)
    - [Add Environment Variables](#add-environment-variables)
    - [Initialize and Run the Postgresql Database](#initialize-and-run-the-postgresql-database)
    - [Resolve the Dependencies and Build the Server](#resolve-the-dependencies-and-build-the-server)
    - [Run the Server](#run-the-server)
- [Build the CROSS Server Docker Image](#build-the-cross-server-docker-image)
    - [Image Prerequisites](#image-prerequisites)
    - [Deploy the Local Dependencies](#deploy-the-local-dependencies)
    - [Build Image](#build-image)
- [Push the CROSS Server Docker Image to the GCloud Registry](#push-the-cross-server-docker-image-to-the-gcloud-registry)
    - [Push Prerequisites](#push-prerequisites)
    - [Push the Image](#push-the-image)
- [Run Locally with Docker Compose](#run-locally-with-docker-compose)
    - [Docker Compose Prerequisites](#docker-compose-prerequisites)
    - [Run Docker Compose](#run-docker-compose)
- [Run Locally with Kubernetes Minikube Cluster](#run-locally-with-kubernetes-minikube-cluster)
    - [Minikube Prerequisites](#minikube-prerequisites)
    - [Start the Minikube Cluster](#start-the-minikube-cluster)
    - [Deploy PostgreSQL](#deploy-postgresql)
    - [Deploy CROSS Server](#deploy-cross-server)
    - [Test Connectivity](#test-connectivity)
- [Deploy to Google Cloud](#deploy-to-google-cloud)
    - [Google Cloud Prerequisites](#google-cloud-prerequisites)
    - [Create the Cluster](#create-the-cluster)
    - [Automatic Deployment](#automatic-deployment)
    - [Deploy PostgreSQL to the Cloud](#deploy-postgresql-to-the-cloud)
    - [Deploy CROSS Server to the Cloud](#deploy-cross-server-to-the-cloud)
    - [NodePort Firewall Rule](#nodeport-firewall-rule)
    - [Delete the GKE Cluster](#delete-the-gke-cluster)
- [(Optional) Submit Location Certificates of Location Claims to the LCT](#optional-submit-location-certificates-of-location-claims-to-the-lct)
- [Authors](#authors)

## Build and Run From Source

### Source Prerequisites

- Java Development Kit (JDK) >= 11
- Maven >= 3.8
- Postgresql >= 14.2
- Build the [SureThing_Core_Data](https://github.com/inesc-id/SureThing_Core_Data)
- Build the [CROSSContract](https://github.com/inesc-id/SureThing_Wireless_Data/tree/main/CROSS-Contract)
- Build the [LedgerContract](https://github.com/inesc-id/SureThing_Transparency_Data/tree/main/Ledger-Contract)
- Build the [Merkle-Tree-Contract](https://github.com/inesc-id/SureThing_Transparency_Data/tree/main/Merkle-Tree-Contract)

### Add Environment Variables

For example:

```shell script
sudo nano ~/.bashrc
```

And add the following lines:

```shell script
export CROSS_SERVER=https://0.0.0.0:8080
export CROSS_DB_CONNECTION=localhost
export CROSS_DB_NAME=cross
export CROSS_DB_USER=cross
```

Restart the terminal session or execute the contents of the _.bashrc_ file with the following command:

```shell script
source ~/.bashrc
```

### Initialize and Run the Postgresql Database

From the root of the project change directory:

```shell script
cd src/main/resources/database
```

Give execute permissions to the initialization script:

```shell script
chmod +x ./newDB.sh
```

Execute the initialization script _(responsible for creating and populating the tables)_:

```shell script
./newDB.sh
```

### Resolve the Dependencies and Build the Server

From the root of the project execute:

```shell script
mvn clean install
```

### Run the Server

```shell script
mvn exec:java
```

## Build the CROSS Server Docker Image

### Image Prerequisites

- Docker >= 20.10.7
- Build the [SureThing_Core_Data](https://github.com/inesc-id/SureThing_Core_Data)
- Build the [SureThing_Signature_Util](https://github.com/inesc-id/SureThing_Core_Util/tree/main/ST-SS)
- Build the [CROSS-Contract](https://github.com/inesc-id/SureThing_CROSS_Data)
- Build the [Ledger-Contract](https://github.com/inesc-id/SureThing_Transparency_Data)

### Deploy the Local Dependencies

From the root of the project create a new directory for the local dependency packages (*SureThing_Core_Data*, *
SureThing_Signature_Util*, *CROSS-Contract* and *Legder-Contract*):

```shell script
mkdir local-maven-repo
```

Deploy the *SureThing_Core_Data* dependency **_(don't forget to replace the parameter -Dfile with the absolute path to
the SureThing_Core_Data .jar)_**:

```shell script
mvn deploy:deploy-file -DgroupId=eu.surething_project.core -DartifactId=surething-data-core -Dversion=0.0.1 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=[PATH_TO_JAR] -Dpackaging=jar
```

Deploy the *SureThing_Signature_Util* dependency **_(don't forget to replace the parameter -Dfile with the absolute path
to the SureThing Signature Util .jar)_**:

```shell script
mvn deploy:deploy-file -DgroupId=eu.surething_project.signature.util -DartifactId=surething-signature-util -Dversion=0.0.1 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=[PATH_TO_JAR] -Dpackaging=jar
```

Deploy the *CROSS-Contract* dependency **_(don't forget to replace the parameter -Dfile with the absolute path to the
CROSSContract .jar)_**:

```shell script
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.cross -DartifactId=CROSS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=[PATH_TO_JAR] -Dpackaging=jar
```

Deploy the *Legder-Contract* dependency **_(don't forget to replace the parameter -Dfile with the absolute path to the
LedgerContract .jar)_**:

```shell script
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.transparency -DartifactId=Ledger-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=[PATH_TO_JAR] -Dpackaging=jar
```

### Build Image

Build the image:

```shell script
docker build -t cross-server .
```

## Push the CROSS Server Docker Image to the GCloud Registry

### Push Prerequisites

- Docker >= 20.10.7
- gcloud >= 333.0.0
- [Build the CROSS Server Docker Image](#build-the-cross-server-docker-image)

### Push the Image

Tag the image, for example for version 1.0.2:

```shell script
docker tag cross-server gcr.io/gsdsupport/cross-server:v1.0.2
```

Push to the registry:

```shell script
gcloud docker -- push gcr.io/gsdsupport/cross-server:v1.0.2
```

## Run Locally with Docker Compose

### Docker Compose Prerequisites

- Docker >= 20.10.7
- [Build the CROSS Server Docker Image](#build-the-cross-server-docker-image)
- docker-compose >= 1.29.2

### Run Docker Compose

From the root of the project, run docker-compose containing both the CROSS Server and CROSS PSQL services _(Use the -d
parameter to detach)_:

```shell script
docker-compose up
```

Whenever you want to stop the services execute:

```shell script
docker-compose down
```

## Run Locally with Kubernetes Minikube Cluster

### Minikube Prerequisites

- Docker >= 20.10.7 (or other Container/VM Manager)
- Minikube >= 1.25.2
- kubectl >= 1.22.2
- [Build the CROSS Server Docker Image](#build-the-cross-server-docker-image)

### Start the Minikube Cluster

```shell script
minikube start
```

### Deploy PostgreSQL

From the root of the project, change directory to k8s/minikube:

```shell script
cd k8s/minikube
```

Create and apply the PostgreSQL secrets:

```shell script
kubectl apply -f psql-secret.yaml
```

Create and apply the PostgreSQL schema init file as a config map:

```shell script
kubectl create configmap cross-psql-schema --from-file=../../src/main/resources/database/1-schema.sql
```

Create and apply the PostgreSQL populate init file as a config map:

```shell script
kubectl create configmap cross-psql-populate --from-file=../../src/main/resources/database/2-populate.sql
```

Create and apply the PostgreSQL persistent volume and persistent volume claim:

```shell script
kubectl apply -f psql-pv.yaml
```

Create and apply the PostgreSQL deployment and service:

```shell script
kubectl apply -f psql.yaml
```

### Deploy CROSS Server

From the root of the project, change directory to k8s/minikube:

```shell script
cd k8s/minikube
```

Make sure you built the docker image in the minikube docker-env for the current terminal session with:

```shell script
eval $(minikube docker-env)
```

Create and apply the CROSS Server secrets:

```shell script
kubectl apply -f cross-secret.yaml
```

Create and apply the CROSS Server config map:

```shell script
kubectl apply -f cross-config.yaml
```

Create and apply the CROSS Server deployment and service:

```shell script
kubectl apply -f cross.yaml
```

### Test Connectivity

Get the minikube cluster ip:

```shell script
minikube ip
```

Contact via:

```shell script
<minikube-ip>:30100
```

## Deploy to Google Cloud

### Google Cloud Prerequisites

- Docker >= 20.10.7 (or other Container/VM Manager)
- kubectl >= 1.22.2
- Helm 3+
- [Push the CROSS Server Docker Image to the GCloud Registry](#push-the-cross-server-docker-image-to-the-gcloud-registry)

### Create the Cluster

Create a regional one node per zone cluster in europe-west1:

```shell script
gcloud container clusters create "cross-gke-regional" \
  --region "europe-west1" \
  --machine-type "e2-standard-2" --disk-type "pd-standard" --disk-size "20" \
  --num-nodes "1" --node-locations "europe-west1-b","europe-west1-c"
```

Get the GKE cluster credentials:

```shell script
gcloud container clusters get-credentials cross-gke-regional --region europe-west1
```

(skip if running automatic deployment) Elevate to "cluster-admin" permissions (needed for Nginx ingress controller
deployment and cert-manager)

```shell script
kubectl create clusterrolebinding cluster-admin-binding \
    --clusterrole=cluster-admin \
    --user=$(gcloud config get-value core/account)
```

### Automatic Deployment

From the root of the project, change directory to k8s/gcp:

```shell script
cd k8s/gcp
```

Make sure you have execute permissions for the deployment script:

```shell script
chmod +x ./deploy.sh
```

Execute the deployment script (requires the external-dns.yaml manifest with the DNS Provider credentials):

```shell script
./deploy.sh
```

### Deploy PostgreSQL to the Cloud

From the root of the project, change directory to k8s/gcp:

```shell script
cd k8s/gcp
```

Create and apply the PostgreSQL secrets:

```shell script
kubectl apply -f psql-secret.yaml
```

Create and apply the PostgreSQL schema init file as a config map:

```shell script
kubectl create configmap cross-psql-schema --from-file=../../src/main/resources/database/1-schema.sql
```

Create and apply the PostgreSQL populate init file as a config map:

```shell script
kubectl create configmap cross-psql-populate --from-file=../../src/main/resources/database/2-populate.sql
```

Create and apply the PostgreSQL persistent volume and persistent volume claim:

```shell script
kubectl apply -f psql-pv.yaml
```

Create and apply the PostgreSQL deployment and service:

```shell script
kubectl apply -f psql.yaml
```

### Deploy CROSS Server to the Cloud

From the root of the project, change directory to k8s/gcp:

```shell script
cd k8s/gcp
```

Create and apply the CROSS Server secrets:

```shell script
kubectl apply -f cross-secret.yaml
```

Create and apply the CROSS Server config map:

```shell script
kubectl apply -f cross-config.yaml
```

Create and apply the CROSS Server Service Account as a secret:

```shell script
kubectl create secret generic cross-key --from-file ~/key.json
```

Create and apply the CROSS Server deployment and service:

```shell script
kubectl apply -f cross.yaml
```

### NodePort Firewall Rule

(Only add this rule when using CROSS Server as a NodePort Service, without the ingress controller)
For external connectivity to CROSS Server, as a NodePort type service, create the following resource:

```shell script
gcloud compute firewall-rules create node-port-sv --allow tcp:30100
```

### Delete the GKE Cluster

Delete the cluster:

```shell script
gcloud container clusters delete "cross-gke-regional" \
  --region "europe-west1"
```

Delete all persistent disks:

```shell script
gcloud compute disks delete $(gcloud compute disks list --format="value(name)" --filter="name~^gke-cross") --region "europe-west1"
```

## (Optional) Submit Location Certificates of Location Claims to the LCT

If an LCT is employed, you can set Location Certificate submission on by:

From the root of the project change directory:

```shell script
cd src/main/resources
```

In the file _CROSSCityServer.properties_ set the following variables:

```properties
LCT=true(Default=false)
LCT_MMD=<maximum-merge-delay>(Default=30000)
LEDGER_URL=<ledger-url>(Default=https://localhost:8443/v2/ledger)
AUDITOR_URL=<auditor-url>(Default=https://localhost:8445/v2/auditor)
```

## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                                                       |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lucas Vicente     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:lucasdhvicente@gmail.com "lucasdhvicente@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/WARSKELETON "WARSKELETON") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/lucas-vicente-a91819184/ "lucas-vicente-a91819184") |
| Rafael Figueiredo | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo")                               |
| Ricardo Grade     | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:ricardo.grade@tecnico.ulisboa.pt "ricardo.grade@tecnico.ulisboa.pt") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/RicardoGrade "RicardoGrade") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/RicardoGrade "RicardoGrade")      |
