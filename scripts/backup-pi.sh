#!/usr/bin/env bash
# =============================================================================
# backup-pi.sh — Daily PostgreSQL backup via Podman (runs on the Pi)
#
# SETUP:
#   1. Make executable:   chmod +x /home/anton/foos/scripts/backup-pi.sh
#   2. Add to crontab:    crontab -e
#      Add this line:
#        0 10 * * * /home/anton/foos/scripts/backup-pi.sh
# =============================================================================

set -euo pipefail

# Required for rootless Podman to find the user's socket when invoked from cron,
# which does not have a full login environment. Without this, podman commands fail.
export XDG_RUNTIME_DIR="/run/user/$(id -u)"

CONTAINER_NAME="foos-db"
DB_USER="postgres"
DB_NAME="foos"
RETAIN_DAYS=7
LOG_FILE="$(dirname "$0")/backup-pi.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILENAME="foos_${TIMESTAMP}.sql.gz"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

# Resolve the Podman volume mountpoint dynamically
VOLUME_NAME="foos_postgres_backups"
BACKUP_DIR=$(podman volume inspect "$VOLUME_NAME" --format '{{.Mountpoint}}' 2>/dev/null || true)

if [[ -z "$BACKUP_DIR" ]]; then
  log "ERROR: Could not find Podman volume '$VOLUME_NAME'. Is the stack running?"
  exit 1
fi

# Check the container is running
if ! podman ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  log "ERROR: Container '$CONTAINER_NAME' is not running."
  exit 1
fi

log "Starting backup → ${BACKUP_DIR}/${BACKUP_FILENAME}"

# Run pg_dump inside the container and compress output
if podman exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" "$DB_NAME" | gzip > "${BACKUP_DIR}/${BACKUP_FILENAME}"; then
  SIZE=$(du -sh "${BACKUP_DIR}/${BACKUP_FILENAME}" | cut -f1)
  log "Backup complete: ${BACKUP_FILENAME} (${SIZE})"
else
  log "ERROR: pg_dump failed."
  rm -f "${BACKUP_DIR}/${BACKUP_FILENAME}"
  exit 1
fi

# Rotate: delete backups older than RETAIN_DAYS
DELETED=$(find "$BACKUP_DIR" -name "foos_*.sql.gz" -mtime +"$RETAIN_DAYS" -print -delete | wc -l | tr -d ' ')
if [[ "$DELETED" -gt 0 ]]; then
  log "Rotated ${DELETED} backup(s) older than ${RETAIN_DAYS} days."
fi

log "Done."
