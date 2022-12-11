#!/bin/bash

pois=("Comercio")
dates=("2019-07-29" "2019-07-30" "2019-07-31" "2019-08-01" "2019-08-02" "2019-08-03" "2019-08-04" "2019-08-19")

for date in ${dates[@]}; do
    for poi in ${pois[@]}; do
        ./playback.sh $poi $date
    done
done
