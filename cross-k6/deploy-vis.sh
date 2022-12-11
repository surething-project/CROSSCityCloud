#!/bin/sh

helm upgrade --install main-grafa grafana/grafana

helm upgrade --install main influxdata/influxdb