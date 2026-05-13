FROM postgres:16

COPY --chown=postgres:postgres ./refresh.sh /docker-entrypoint-initdb.d