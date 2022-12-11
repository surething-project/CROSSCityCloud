#!/bin/sh
mkdir -p local-maven-repo
docker image rm cross-server
docker image rmi gcr.io/gsdsupport/cross-server-deployed:v1.0.0


# Copy CA-Key
cp ${JAVA_HOME}/lib/security/cacerts cacerts


ln -s ~/.m2/repository/eu/surething_project/core/surething-data-core/0.0.1/surething-data-core-0.0.1.jar surething-data-core.jar
ln -s ~/.m2/repository/eu/surething_project/signature/util/surething-signature-util/0.0.1/surething-signature-util-0.0.1.jar surething-signature-util.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/cross/CROSS-Contract/1.0-SNAPSHOT/CROSS-Contract-1.0-SNAPSHOT.jar cross-contract.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/transparency/Ledger-Contract/1.0-SNAPSHOT/Ledger-Contract-1.0-SNAPSHOT.jar ledger-contract.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/CA-Contract/1.0-SNAPSHOT/CA-Contract-1.0-SNAPSHOT.jar surerepute-ca.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/SureRepute-CS-Contract/1.0-SNAPSHOT/SureRepute-CS-Contract-1.0-SNAPSHOT.jar surerepute-cs.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/IdentityProvider-Contract/1.0-SNAPSHOT/IdentityProvider-Contract-1.0-SNAPSHOT.jar surerepute-ip.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/SureRepute-Client/1.0-SNAPSHOT/SureRepute-Client-1.0-SNAPSHOT.jar surerepute-client.jar

mvn deploy:deploy-file -DgroupId=eu.surething_project.core -DartifactId=surething-data-core -Dversion=0.0.1 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surething-data-core.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=eu.surething_project.signature.util -DartifactId=surething-signature-util -Dversion=0.0.1 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surething-signature-util.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.cross -DartifactId=CROSS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=cross-contract.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.transparency -DartifactId=Ledger-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=ledger-contract.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=CA-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surerepute-ca.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=IdentityProvider-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surerepute-ip.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=SureRepute-CS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surerepute-cs.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=SureRepute-Client -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=surerepute-client.jar -Dpackaging=jar

docker build -t cross-server .

rm cacerts
rm *.jar