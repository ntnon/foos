#!/usr/bin/env bash
# =============================================================================
# backup-pull-mac.sh — Pull daily backups from the Pi to this Mac
#
# PREREQUISITES:
#   1. Set up SSH key auth from this Mac to the Pi (see below).
#   2. Install this launchd agent so it runs daily at 10:05:
#        cp scripts/com.foos.backup.plist ~/Library/LaunchAgents/
#        launchctl load ~/Library/LaunchAgents/com.foos.backup.plist
#   3. To run manually:  bash /path/to/foos/scripts/backup-pull-mac.sh
#
# SSH KEY SETUP (one-time, run these on your Mac):
#   ssh-keygen -t ed25519 -C "mac-to-rohan-backup"   # skip if you have a key
#   ssh-copy-id anton@rohan                           # copies your public key
#   ssh anton@rohan "echo ok"                         # should print 'ok', no password
#
# HOW IT WORKS:
#   - SSHes into anton@rohan to find the Podman backup volume path
#   - rsyncs any new .sql.gz files from the Pi to ~/foosball-backups/
#   - Deletes local backups older than 30 days
#   - Logs to ~/foosball-backups/pull.log
# =============================================================================

set -euo pipefail

PI_HOST="anton@rohan"
RETAIN_DAYS=30
LOCAL_BACKUP_DIR="${HOME}/foosball-backups"
LOG_FILE="${LOCAL_BACKUP_DIR}/pull.log"
VOLUME_NAME="foos_postgres_backups"

mkdir -p "$LOCAL_BACKUP_DIR"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

log "=== Starting backup pull from ${PI_HOST} ==="

# Resolve the remote backup volume path on the Pi
REMOTE_BACKUP_DIR=$(ssh "$PI_HOST" "podman volume inspect ${VOLUME_NAME} --format '{{.Mountpoint}}'" 2>/dev/null || true)

if [[ -z "$REMOTE_BACKUP_DIR" ]]; then
  log "ERROR: Could not resolve Podman volume '${VOLUME_NAME}' on ${PI_HOST}."
  log "       Is the foos stack running on the Pi? Is SSH key auth set up?"
  exit 1
fi

log "Remote backup dir: ${REMOTE_BACKUP_DIR}"

# rsync: pull only .sql.gz files, archive mode, skip files already present (checksum)
RSYNC_OUTPUT=$(rsync -avz --checksum \
  --include="foos_*.sql.gz" \
  --exclude="*" \
  "${PI_HOST}:${REMOTE_BACKUP_DIR}/" \
  "${LOCAL_BACKUP_DIR}/" 2>&1)

# Count newly transferred files
NEW_FILES=$(echo "$RSYNC_OUTPUT" | grep -c "foos_.*\.sql\.gz" || true)
log "rsync complete. ${NEW_FILES} file(s) transferred."

# Rotate: delete local backups older than RETAIN_DAYS
DELETED=$(find "$LOCAL_BACKUP_DIR" -name "foos_*.sql.gz" -mtime +"$RETAIN_DAYS" -print -delete | wc -l | tr -d ' ')
if [[ "$DELETED" -gt 0 ]]; then
  log "Rotated ${DELETED} local backup(s) older than ${RETAIN_DAYS} days."
fi

# Show current local backup count and total size
COUNT=$(find "$LOCAL_BACKUP_DIR" -name "foos_*.sql.gz" | wc -l | tr -d ' ')
SIZE=$(du -sh "$LOCAL_BACKUP_DIR" | cut -f1)
log "Local backups: ${COUNT} file(s), ${SIZE} total."
log "=== Done ==="
