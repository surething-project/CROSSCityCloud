#!/bin/sh
# CA Signs a CSR
openssl x509 -req -days 3650 -in CROSSCityServer.csr -CA ../../../../../CA/CA.crt -CAkey ../../../../../CA/CA.key -out CROSSCityServer.crt -extensions req_ext -extfile CROSSCityServer.cnf
# Generate Key Store
openssl pkcs12 -export -in CROSSCityServer.crt -name CROSSCityServerCrt -inkey CROSSCityServer.key -out CROSSCityServer.p12
