# FERKO 2.0 (Modernized Academic Portal)

FERKO is a modernized, production-oriented rewrite path of the historical JCMS/FERKO academic platform used at the Faculty of Electrical Engineering and Computing (FER), University of Zagreb.

This repository now runs as a Java 21 + Spring Boot + PostgreSQL + Docker system with seeded FERKO academic workflows visible immediately in the browser.

## 1. What You Get

When you start the app and open `http://localhost:8080`, you get a role-based FERKO portal, not a blank CRUD demo.

Implemented browser surface includes:

- Login page with FERKO branding and role-aware redirect.
- Role dashboards for:
  - `STUDENT`
  - `LECTURER`
  - `ASSISTANT`
  - `STUSLU` (student office)
  - `ADMIN`
- Core academic modules:
  - semester lifecycle
  - course/staff/group management
  - student enrollment + JMBAG-centric records
  - lecture/lab schedules
  - points + grading overview
  - exam organization + publishing
  - group exchange workflow
  - sync operation visibility

The portal is pre-initialized with modern seed data and enriched with imported historical datasets from `course-isvu-data` and `noviPodatci`.

## 2. Quick Start (Docker, Recommended)

### Start

```bash
./scripts/dev-up.sh
```

### Open

- UI: `http://localhost:8080`
- OpenAPI docs: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`

### Stop

```bash
./scripts/dev-down.sh
```

### Full reset (drop DB volume)

```bash
./scripts/dev-reset.sh
```

## 3. Local Demo Accounts

All demo users use password `ferko123`.

- `student.ana`
- `lecturer.marko`
- `assistant.iva`
- `stuslu.sara`
- `admin.ferko`

## 4. Apple Silicon (M1/M2/M3)

Intel CPU is not required.

- Docker setup and GHCR pipeline are multi-arch (`linux/amd64`, `linux/arm64`).
- Local Apple Silicon flow works with the standard commands above.

## 5. Project Structure

```text
backend/
  ferko-domain/              # Domain model and core business value objects
  ferko-application/         # Use cases and ports (hexagonal application layer)
  ferko-infrastructure/      # JDBC adapters and persistence implementations
  ferko-security/            # Security module boundary
  ferko-web-api/             # Spring Boot app, REST API, portal web UI, Flyway
  ferko-architecture-tests/  # ArchUnit rules for module boundaries
build-tools/
  checkstyle/
  dependency-check/
docs/
  getting-started/
  architecture/
  operations/
  modernization/
  legacy/
scripts/
.github/workflows/
```

## 6. Modern Stack

- Java 21
- Maven multi-module build
- Spring Boot 3
- Spring Security (JWT/OIDC resource-server model)
- PostgreSQL 16 (docker), Flyway migrations
- OpenAPI (springdoc)
- Actuator health endpoints
- Docker multi-stage image

## 7. Data Initialization and Bootstrap

### 7.1 Schema and technical data

Flyway migrations initialize:

- `todo_tasks`
- `todo_audit_log`
- `legacy_bootstrap_*` import tables for historical datasets

Migrations are under:

- `backend/ferko-web-api/src/main/resources/db/migration`

### 7.2 Historical FERKO dataset ingestion

At startup, application ingests packaged dataset resources:

- `bootstrap/course-isvu-data/*`
- `bootstrap/noviPodatci/*.txt`

Imported into DB tables:

- `legacy_bootstrap_course`
- `legacy_bootstrap_enrollment`
- `legacy_bootstrap_schedule`
- `legacy_bootstrap_exam`
- `legacy_bootstrap_raw_line`

### 7.3 Portal pre-initialization

Portal service merges imported legacy data into visible FERKO workspace:

- course catalog expansion
- student enrollments/groups
- schedule entries
- exam terms
- additional grading and exchange activity

Result: app starts in a realistic, non-empty academic state.

## 8. Configuration (Unified Profiles)

All runtime config is unified in:

- `backend/ferko-web-api/src/main/resources/application.yml`

Profiles:

- default: local/dev baseline (H2 fallback + bootstrap enabled)
- `docker`: PostgreSQL container profile
- `staging`/`prod`: hardened mode (no dev token fallback, no HMAC fallback)

### Key environment variables

- Database:
  - `FERKO_DB_URL`
  - `FERKO_DB_USERNAME`
  - `FERKO_DB_PASSWORD`
  - `FERKO_DB_DRIVER`
- Security/JWT:
  - `FERKO_OIDC_ISSUER_URI`
  - `FERKO_OIDC_JWK_SET_URI`
  - `FERKO_JWT_HMAC_SECRET`
  - `FERKO_JWT_ALLOW_HMAC_DECODER`
  - `FERKO_DEV_TOKEN_ENABLED`
- Repository adapters:
  - `FERKO_TODO_REPOSITORY`
  - `FERKO_AUDIT_REPOSITORY`
- Bootstrap controls:
  - `FERKO_BOOTSTRAP_LEGACY_ENABLED`
  - `FERKO_PORTAL_BOOTSTRAP_ENABLED`
  - `FERKO_PORTAL_BOOTSTRAP_MAX_COURSES`
  - `FERKO_PORTAL_BOOTSTRAP_MAX_STUDENTS`
  - `FERKO_PORTAL_BOOTSTRAP_MAX_SCHEDULE_ENTRIES`
  - `FERKO_PORTAL_BOOTSTRAP_MAX_EXAM_ENTRIES`

## 9. Security Model

- ToDo API endpoints are protected by OAuth2 resource-server JWT validation.
- Principal identity is derived from authenticated JWT claims (not userId query params).
- Privileged ToDo actions and denied attempts are audit-logged in DB.
- Staging/prod guardrail enforces startup failure if OIDC/JWK decoder config is missing.
- Dev token issuing endpoint is restricted to non-staging/non-prod profiles.

## 10. Build, Quality, and CI/CD

### Local quality gate

```bash
./mvnw -B -ntp verify
```

This runs tests, formatting, static checks, architecture tests, and coverage checks.

### CI highlights

- Spotless + Checkstyle + JaCoCo
- OWASP dependency vulnerability scan
- Dependency inventory artifacts
- Container smoke tests
- Container vulnerability scan (Trivy)
- staging auth hardening smoke guardrail
- GHCR release publishing with semantic + immutable SHA tags

Workflows:

- `/.github/workflows/maven-phase1.yml`
- `/.github/workflows/release-image-ghcr.yml`

## 11. Documentation Map

Start here:

- `docs/README.md`
- `docs/getting-started/QUICKSTART.md`
- `docs/getting-started/INSTALLATION.md`
- `docs/getting-started/DATA_INITIALIZATION.md`

Legacy translated references:

- `docs/legacy/HOW_TO_INSTALL_EN.md`
- `docs/legacy/INITIALIZE_ALL_DATA_EN.md`
- `docs/legacy/LOAD_DATA_EN.md`

## 12. Operational Notes

- Default `docker-compose` starts `ferko-app` + `postgres`.
- Optional profiles include `redis` and `mailhog`.
- App is served from a single Spring Boot process with static frontend assets.
- For production-style deployment, prefer externalized secrets + OIDC/JWK config + hardened profile.

## 13. License

See `LICENSE` and `NOTICE`.
