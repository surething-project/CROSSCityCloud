#!/bin/bash

date=$1
poi=$2

du -sh -- ../../../Tese/cross-field-experiments/$date/$poi/alice.txt ../../../Tese/cross-field-experiments/$date/$poi/bob.txt ../../../Tese/cross-field-experiments/$date/$poi/charlie.txt | awk '{print $1}' | sed 's/.$//' | paste -sd+ - | bc
