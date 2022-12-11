#!/bin/sh

DB_NAME="cross"
DB_USER="cross"
DB_PWD="cross"
DB_SCHEMA="1-schema.sql"
DB_POPULATE="2-populate.sql"

CMD="
dropdb --if-exists ${DB_NAME};
createdb ${DB_NAME};
echo \"
  DROP ROLE IF EXISTS ${DB_USER};
  CREATE ROLE ${DB_USER} LOGIN SUPERUSER PASSWORD '${DB_PWD}';
\" | psql ${DB_NAME};
"

sudo service postgresql start
echo "${CMD}" | sudo su -l postgres

psql -d ${DB_NAME} -f ${DB_SCHEMA}
psql -d ${DB_NAME} -f ${DB_POPULATE}
