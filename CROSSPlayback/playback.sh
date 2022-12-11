#!/bin/bash

users=("alice" "bob" "charlie")
poi=$1
date=$2

for user in ${users[@]}; do
    mvn exec:java -Dexec.args="FILE ../../../Tese/cross-field-experiments '${date}' '${poi}' '${user}' 0 SCAVENGER" &
done
