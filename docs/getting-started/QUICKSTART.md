# Quick Start

This is the fastest way to run FERKO locally and inspect full role-based workflows in browser.

## 1) Start stack

```bash
./scripts/dev-up.sh
```

This starts:

- `ferko-app` (Spring Boot API + web frontend)
- `postgres`

## 2) Open browser

- Portal UI: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 3) Login (demo users)

Password for all users: `ferko123`

1. `student.ana` (student dashboard, grades, exchange requests)
2. `lecturer.marko` (teaching, points entry, exams)
3. `assistant.iva` (lab-focused course workflows)
4. `stuslu.sara` (enrollment/group assignment/student-office actions)
5. `admin.ferko` (semester/course/staff/sync governance)

## 4) Stop or reset

Stop:

```bash
./scripts/dev-down.sh
```

Reset DB volume:

```bash
./scripts/dev-reset.sh
```

## Notes

- App starts pre-initialized with seeded FERKO-like academic data.
- Historical datasets from `course-isvu-data` and `noviPodatci` are loaded automatically.
- Apple Silicon (M1/M2/M3) is supported; Intel-only Docker is not required.
