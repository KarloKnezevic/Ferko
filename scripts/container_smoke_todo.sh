#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${FERKO_BASE_URL:-http://localhost:8080}"
JWT_SECRET="${FERKO_JWT_HMAC_SECRET:-ferko-dev-jwt-hmac-secret-0123456789abcdef}"
OUT_DIR="${FERKO_SMOKE_OUT_DIR:-target/container-smoke}"
DB_USER="${FERKO_DB_USERNAME:-ferko}"
DB_NAME="${FERKO_DB_NAME:-ferko}"
POSTGRES_SERVICE="${FERKO_POSTGRES_SERVICE:-postgres}"
SKIP_DB_AUDIT_CHECK="${FERKO_SKIP_DB_AUDIT_CHECK:-false}"

OWNER_ID=31
ASSIGNEE_ID=41
OUTSIDER_ID=999

mkdir -p "${OUT_DIR}"

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

b64url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

jwt_token() {
  local subject="$1"
  local scope="$2"
  local now exp header payload signing_input signature
  now="$(date +%s)"
  exp="$((now + 900))"
  header='{"alg":"HS256","typ":"JWT"}'
  payload="$(printf '{"sub":"%s","iss":"ferko-container-smoke","iat":%s,"exp":%s,"scope":"%s"}' "${subject}" "${now}" "${exp}" "${scope}")"
  signing_input="$(printf '%s' "${header}" | b64url).$(printf '%s' "${payload}" | b64url)"
  signature="$(printf '%s' "${signing_input}" | openssl dgst -binary -sha256 -hmac "${JWT_SECRET}" | b64url)"
  printf '%s.%s' "${signing_input}" "${signature}"
}

http_json() {
  local method="$1"
  local url="$2"
  local out_file="$3"
  local auth_token="${4:-}"
  local payload="${5:-}"
  local -a args
  args=(-sS -X "${method}" "${url}" -o "${out_file}" -w "%{http_code}")
  if [[ -n "${auth_token}" ]]; then
    args+=(-H "Authorization: Bearer ${auth_token}")
  fi
  if [[ -n "${payload}" ]]; then
    args+=(-H "Content-Type: application/json" --data "${payload}")
  fi
  curl "${args[@]}"
}

assert_http() {
  local actual="$1"
  local expected="$2"
  local context="$3"
  local body_file="$4"
  if [[ "${actual}" != "${expected}" ]]; then
    echo "Expected HTTP ${expected}, got ${actual} for ${context}." >&2
    if [[ -f "${body_file}" ]]; then
      echo "Response body (${body_file}):" >&2
      cat "${body_file}" >&2
    fi
    exit 1
  fi
}

json_get() {
  local file="$1"
  local expr="$2"
  if command -v jq >/dev/null 2>&1; then
    jq -r "${expr}" "${file}"
    return
  fi
  python3 - "$file" "$expr" <<'PY'
import json
import sys

path = sys.argv[2]
obj = json.load(open(sys.argv[1], "r", encoding="utf-8"))
parts = path.strip(".").split(".")
for part in parts:
    if not part:
        continue
    if isinstance(obj, list):
        obj = obj[int(part)]
    else:
        obj = obj[part]
if isinstance(obj, bool):
    print("true" if obj else "false")
elif obj is None:
    print("null")
else:
    print(obj)
PY
}

OWNER_RW_TOKEN="$(jwt_token "${OWNER_ID}" "todo.read todo.write")"
ASSIGNEE_RW_TOKEN="$(jwt_token "${ASSIGNEE_ID}" "todo.read todo.write")"
OUTSIDER_RW_TOKEN="$(jwt_token "${OUTSIDER_ID}" "todo.read todo.write")"
OWNER_READ_ONLY_TOKEN="$(jwt_token "${OWNER_ID}" "todo.read")"

CREATE_PAYLOAD='{"assigneeId":41,"title":"Container smoke task","description":"Validate authenticated ToDo flow.","deadline":"2026-03-20T10:15:00","priority":"MEDIUM"}'
READ_ONLY_CREATE_PAYLOAD='{"assigneeId":41,"title":"Read-only denied create","description":"Should be denied by scope.","deadline":"2026-03-21T10:15:00","priority":"TRIVIAL"}'

CREATE_STATUS="$(http_json "POST" "${BASE_URL}/api/v1/todo/tasks" "${OUT_DIR}/create.json" "${OWNER_RW_TOKEN}" "${CREATE_PAYLOAD}")"
assert_http "${CREATE_STATUS}" "201" "create task" "${OUT_DIR}/create.json"

TASK_ID="$(json_get "${OUT_DIR}/create.json" ".id")"
[[ "${TASK_ID}" =~ ^[0-9]+$ ]] || fail "Could not parse numeric task id from create response."

MY_STATUS="$(http_json "GET" "${BASE_URL}/api/v1/todo/my" "${OUT_DIR}/my-open.json" "${ASSIGNEE_RW_TOKEN}")"
assert_http "${MY_STATUS}" "200" "list my open tasks" "${OUT_DIR}/my-open.json"
grep -q "\"id\":${TASK_ID}" "${OUT_DIR}/my-open.json" || fail "Created task missing from assignee /my list."

ASSIGNED_STATUS="$(http_json "GET" "${BASE_URL}/api/v1/todo/assigned" "${OUT_DIR}/assigned-open.json" "${OWNER_RW_TOKEN}")"
assert_http "${ASSIGNED_STATUS}" "200" "list assigned open tasks" "${OUT_DIR}/assigned-open.json"
grep -q "\"id\":${TASK_ID}" "${OUT_DIR}/assigned-open.json" || fail "Created task missing from owner /assigned list."

DENIED_CLOSE_STATUS="$(http_json "POST" "${BASE_URL}/api/v1/todo/tasks/${TASK_ID}/close" "${OUT_DIR}/close-denied.json" "${OUTSIDER_RW_TOKEN}")"
assert_http "${DENIED_CLOSE_STATUS}" "403" "close task as unrelated actor" "${OUT_DIR}/close-denied.json"

CLOSE_STATUS="$(http_json "POST" "${BASE_URL}/api/v1/todo/tasks/${TASK_ID}/close" "${OUT_DIR}/close-success.json" "${ASSIGNEE_RW_TOKEN}")"
assert_http "${CLOSE_STATUS}" "200" "close task as assignee" "${OUT_DIR}/close-success.json"
CLOSE_OUTCOME="$(json_get "${OUT_DIR}/close-success.json" ".status")"
[[ "${CLOSE_OUTCOME}" == "CLOSED" ]] || fail "Expected CLOSED status after close, got ${CLOSE_OUTCOME}."

MY_AFTER_CLOSE_STATUS="$(http_json "GET" "${BASE_URL}/api/v1/todo/my" "${OUT_DIR}/my-after-close.json" "${ASSIGNEE_RW_TOKEN}")"
assert_http "${MY_AFTER_CLOSE_STATUS}" "200" "list /my after close" "${OUT_DIR}/my-after-close.json"
if [[ "$(tr -d '[:space:]' < "${OUT_DIR}/my-after-close.json")" != "[]" ]]; then
  fail "Expected empty /my list for assignee after close."
fi

DENIED_CREATE_STATUS="$(http_json "POST" "${BASE_URL}/api/v1/todo/tasks" "${OUT_DIR}/create-denied.json" "${OWNER_READ_ONLY_TOKEN}" "${READ_ONLY_CREATE_PAYLOAD}")"
assert_http "${DENIED_CREATE_STATUS}" "403" "create with read-only token" "${OUT_DIR}/create-denied.json"

UNAUTH_CREATE_STATUS="$(http_json "POST" "${BASE_URL}/api/v1/todo/tasks" "${OUT_DIR}/create-unauthenticated.json" "" "${READ_ONLY_CREATE_PAYLOAD}")"
assert_http "${UNAUTH_CREATE_STATUS}" "401" "unauthenticated create" "${OUT_DIR}/create-unauthenticated.json"

if [[ "${SKIP_DB_AUDIT_CHECK}" == "true" ]]; then
  echo "Skipping database audit verification because FERKO_SKIP_DB_AUDIT_CHECK=true."
  echo "Container ToDo smoke checks passed."
  exit 0
fi

docker compose exec -T "${POSTGRES_SERVICE}" \
  psql -U "${DB_USER}" -d "${DB_NAME}" -At \
  -c "select action, outcome, coalesce(actor_user_id::text, 'NULL'), coalesce(task_id::text, 'NULL') from todo_audit_log order by id;" \
  > "${OUT_DIR}/audit-log-rows.txt"

grep -q "^CREATE|SUCCESS|${OWNER_ID}|${TASK_ID}$" "${OUT_DIR}/audit-log-rows.txt" \
  || fail "Missing CREATE/SUCCESS audit row for created task ${TASK_ID}."
grep -q "^CLOSE|DENIED|${OUTSIDER_ID}|${TASK_ID}$" "${OUT_DIR}/audit-log-rows.txt" \
  || fail "Missing CLOSE/DENIED audit row for unauthorized actor ${OUTSIDER_ID} on task ${TASK_ID}."
grep -q "^CLOSE|SUCCESS|${ASSIGNEE_ID}|${TASK_ID}$" "${OUT_DIR}/audit-log-rows.txt" \
  || fail "Missing CLOSE/SUCCESS audit row for assignee ${ASSIGNEE_ID} on task ${TASK_ID}."
grep -q "^CREATE|DENIED|${OWNER_ID}|NULL$" "${OUT_DIR}/audit-log-rows.txt" \
  || fail "Missing CREATE/DENIED audit row for read-only token create attempt."
grep -q "^CREATE|DENIED|NULL|NULL$" "${OUT_DIR}/audit-log-rows.txt" \
  || fail "Missing CREATE/DENIED audit row for unauthenticated create attempt."

echo "Container ToDo smoke checks passed."
