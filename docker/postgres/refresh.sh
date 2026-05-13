#!/bin/sh
set -e

BACKUP_DIR=${BACKUP_DIR:-/backups}
BACKUP_SUFFIX=${BACKUP_SUFFIX:-.sql.gz}

LATEST=$(find "${BACKUP_DIR}/last" -maxdepth 1 -type f -name "*${BACKUP_SUFFIX}" -print0 | xargs -0 ls -t | head -n 1)

if [ -n "${LATEST}" ]; then
  echo "Restoring from ${LATEST}"

  if gzip -t "${LATEST}" 2>/dev/null; then
    zcat "${LATEST}" | psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
  else
    cat "${LATEST}" | psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
  fi
fi