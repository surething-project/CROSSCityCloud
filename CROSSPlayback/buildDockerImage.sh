#!/bin/sh
mkdir -p local-maven-repo

ln -s ~/.m2/repository/pt/ulisboa/tecnico/cross/CROSS-Contract/1.0-SNAPSHOT/CROSS-Contract-1.0-SNAPSHOT.jar cross-contract.jar

cp -r ../../../Tese/cross-field-experiments ./cross-field-experiments

mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.cross -DartifactId=CROSS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=cross-contract.jar -Dpackaging=jar

docker build -t cross-playback . -f Dockerfile

rm *.jar
rm -rf ./cross-field-experiments