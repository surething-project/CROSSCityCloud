#!/bin/bash
if [[ $* == *-init* ]]; then
  INIT=1
fi
if [[ $* == *-repute* ]]; then
  REPUTE=1
fi

##################
# Run SureRepute #
##################
if [ "$REPUTE" ]; then
  cd ../../SureRepute || exit
  if [ "$INIT" ]; then
    ./initialSetup.sh 1
    cd Modules/SureRepute-Client || exit
    mvn clean install -DskipTests
    cd - || exit
  fi
  ./run.sh 1
  cd ../CROSSCityCloud/CROSSCityServer || exit
fi

#############
# Run CROSS #
#############
export CROSS_SERVER=https://0.0.0.0:8080
export CROSS_DB_CONNECTION=localhost
export CROSS_DB_NAME=cross
export CROSS_DB_USER=cross
if [ "$INIT" ]; then
  cd src/main/resources/database || exit
  ./newDB.sh
  cd - || exit
fi
mvn clean compile exec:java
