#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd docker
require_cmd curl

echo "Starting Ferko modernized stack (ferko-app + postgres)..."
docker compose up -d --build ferko-app postgres

echo "Waiting for API readiness at http://localhost:8080/actuator/health ..."
for attempt in $(seq 1 90); do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "Ferko is ready."
    break
  fi
  if [[ "${attempt}" -eq 90 ]]; then
    echo "Ferko did not become ready in time. Showing logs:" >&2
    docker compose logs --tail=200 ferko-app postgres >&2 || true
    exit 1
  fi
  sleep 2
done

echo
echo "Open in browser:"
echo "  UI:        http://localhost:8080"
echo "  Swagger:   http://localhost:8080/swagger-ui/index.html"
echo "  Health:    http://localhost:8080/actuator/health"
echo "  Notes:     Legacy FERKO datasets are preloaded into DB and portal views."
echo
echo "To stop: ./scripts/dev-down.sh"
echo "To reset DB: ./scripts/dev-reset.sh"
