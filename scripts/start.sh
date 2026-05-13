#!/usr/bin/env bash
# =============================================================================
# start.sh — Build and start the foos stack
#
# Usage:
#   ./scripts/start.sh          # start (no rebuild)
#   ./scripts/start.sh --build  # force rebuild of images first
#
# Run from the repo root or from anywhere (script resolves its own path).
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "$REPO_ROOT"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

if [[ "${1:-}" == "--build" ]]; then
  log "Building images..."
  podman-compose build
fi

log "Starting foos stack..."
podman-compose up -d

log "Waiting for foos-db to be healthy..."
for i in $(seq 1 30); do
  STATUS=$(podman inspect foos-db --format '{{.State.Health.Status}}' 2>/dev/null || echo "missing")
  if [[ "$STATUS" == "healthy" ]]; then
    break
  fi
  if [[ "$i" == "30" ]]; then
    log "ERROR: foos-db did not become healthy in time."
    exit 1
  fi
  sleep 2
done

log "Stack is up:"
podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep foos
