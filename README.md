# FERKO 2.0

Modernized rewrite of the **FERKO** academic portal of the Faculty of Electrical
Engineering and Computing (FER), University of Zagreb — historically the JCMS platform.

FERKO 2.0 is a full-stack system: a **React + TypeScript** single-page application served by a
**Java 21 + Spring Boot 3** backend over **PostgreSQL**, with exam scheduling powered by an
**evolutionary algorithm** based on Marko Čupić's doctoral thesis *"Raspoređivanje nastavnih
aktivnosti evolucijskim računanjem"* (2011). The whole system starts with a single Docker command
and comes pre-seeded with real FER course data.

## 1. Quick start (Docker)

```bash
./scripts/dev-up.sh           # builds the image (SPA + backend) and starts app + PostgreSQL
```

Then open:

- **App (SPA):** http://localhost:8080
- **OpenAPI / Swagger:** http://localhost:8080/swagger-ui/index.html
- **Health:** http://localhost:8080/actuator/health

Stop / reset:

```bash
./scripts/dev-down.sh         # stop
./scripts/dev-reset.sh        # stop and drop the database volume
```

> The image is multi-arch (`linux/amd64`, `linux/arm64`) and runs natively on Apple Silicon.

## 2. Demo accounts

All demo users share the password **`ferko123`**:

| Korisnik         | Uloga                  |
|------------------|------------------------|
| `admin.ferko`    | ADMIN                  |
| `lecturer.marko` | NASTAVNIK + NOSITELJ   |
| `assistant.iva`  | ASISTENT               |
| `stuslu.sara`    | STUSLU (stud. služba)  |
| `student.ana`    | STUDENT                |

## 3. What you get

On startup the database is seeded with a real FER course catalogue, students, enrollments,
groups and rooms, so the application opens in a realistic, non-empty state:

- **Prijava** i role-aware sučelje (FER branding).
- **Kolegiji** — katalog, detalji, nastavno osoblje, grupe, broj upisa.
- **Administracija provjera znanja** — definiranje provjera (međuispit, završni, …),
  rezervacija dvorana, dohvat (prijava) upisanih studenata.
- **Generiranje rasporeda ispita** — raspoređivanje studenata po dvoranama
  **genetskim algoritmom** ili jednom od četiri FERKO strategije
  (sortirano/slučajno × pohlepno/proporcionalno), pregled po dvoranama i **objava**.
- **Prostorije** i **studenti** (za ovlaštene uloge).

## 4. Exam scheduling (Čupić)

The `ferko-scheduling` module is a self-contained engine that mirrors the thesis:

- **Chromosome** = `int[]` (an option index per item), penalty ≥ 0 (0 = perfect).
- **`GeneticAlgorithm`** — steady-state (elimination) GA, deterministic for a fixed seed.
- **`SeatingProblem`** — student→room assignment minimizing non-linear over-capacity
  `Σ max(0, load − capacity)^α`.
- **`ExamTimetableProblem`** — exam→time-slot assignment minimizing student conflicts.
- **`SeatingStrategies`** — the four deterministic FERKO fill modes.

It is exposed through `/api/v1/academic/exams/{id}/seating` and driven from the
"Administracija provjera znanja" screen.

## 5. Modern stack

- Java 21, Maven multi-module build
- Spring Boot 3.5, Spring Security (form-login/session locally, OIDC resource-server for prod)
- PostgreSQL 16 + Flyway migrations
- React 18 + TypeScript + Vite (built into the jar via `frontend-maven-plugin`)
- OpenAPI (springdoc), Actuator
- Docker multi-stage, multi-arch image

## 6. Project structure

```text
backend/
  ferko-domain/              # domain aggregates / value objects
  ferko-application/         # use cases, ports, view DTOs (hexagonal application layer)
  ferko-infrastructure/      # JDBC adapters
  ferko-security/            # security module boundary
  ferko-scheduling/          # evolutionary scheduling engine (Čupić)
  ferko-web-api/             # Spring Boot app, REST API, Flyway, serves the SPA
  ferko-architecture-tests/  # ArchUnit module-boundary rules
frontend/                    # React + TypeScript SPA (Vite)
docs/                        # architecture, getting-started, operations, ADRs
scripts/                     # dev-up / dev-down / dev-reset / smoke
.github/workflows/           # CI + GHCR release
```

## 7. Build, test, develop

Full backend quality gate (tests, Spotless, Checkstyle, JaCoCo, ArchUnit, SPA build):

```bash
./mvnw -B -ntp verify
```

Frontend with hot reload (proxies the API to `localhost:8080`):

```bash
cd frontend && npm install && npm run dev      # http://localhost:5173
```

## 8. Architecture & data model

- Hexagonal layering enforced by ArchUnit: domain depends on nothing; the web layer never
  imports domain types directly (it consumes application-layer views).
- Flyway migrations define the academic schema: users/roles, semesters, courses, staff,
  students, enrollments, groups, rooms, exams (registrations, rooms, seating), grade components,
  points, grades, group-exchange requests and audit.
- On startup `AcademicDataSeeder` ingests packaged FER datasets into the real tables
  (disabled in `staging`/`prod`).

## 9. Security

- Browser sessions use Spring Security **form-login** backed by the `app_user` table (BCrypt),
  with `ROLE_*` authorities and method-level checks on privileged actions.
- The `staging`/`prod` profiles disable demo seeding and dev tokens, require OIDC/JWK
  configuration, and harden session cookies.

## 10. CI/CD

- CI (`.github/workflows/maven-phase1.yml`): `verify` (incl. SPA build), OWASP dependency scan,
  container build + smoke test, Trivy image scan (HIGH/CRITICAL gate), staging auth guardrail,
  multi-arch build, SBOM/dependency inventory.
- Release (`.github/workflows/release-image-ghcr.yml`): multi-arch GHCR publish on `v*.*.*` tags.

## 11. License

See `LICENSE` and `NOTICE`.
