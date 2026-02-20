#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

docker compose ps

echo
echo "Health endpoint:"
if curl -fsS http://localhost:8080/actuator/health; then
  echo
else
  echo "Unavailable"
fi

echo
echo "Ping endpoint:"
if curl -fsS http://localhost:8080/api/v1/ping; then
  echo
else
  echo "Unavailable"
fi
