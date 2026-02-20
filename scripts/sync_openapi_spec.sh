#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

./mvnw -B -ntp verify

mkdir -p docs/api
cp backend/ferko-web-api/target/openapi/openapi.yaml docs/api/openapi.yaml
cp backend/ferko-web-api/target/openapi/openapi.json docs/api/openapi.json

echo "OpenAPI specs synced to docs/api/openapi.{yaml,json}"
