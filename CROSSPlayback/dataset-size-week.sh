#!/bin/bash

poi=$1

(./dataset-size.sh 2019-07-29 $poi && ./dataset-size.sh 2019-07-30 $poi && ./dataset-size.sh 2019-07-31 $poi && ./dataset-size.sh 2019-08-01 $poi && ./dataset-size.sh 2019-08-02 $poi && ./dataset-size.sh 2019-08-03 $poi && ./dataset-size.sh 2019-08-04 $poi && ./dataset-size.sh 2019-08-19 $poi) | paste -sd+ - | bc
