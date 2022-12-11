#!/bin/sh
# Generates a New Private Key
openssl genrsa -out CROSSCityServer.key 4096
# Generates a New CSR
openssl req -new -key CROSSCityServer.key -out CROSSCityServer.csr -config CROSSCityServer.cnf
