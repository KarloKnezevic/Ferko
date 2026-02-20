# Data Initialization

## Modern Runtime

FERKO initializes data automatically on startup.

## 1) Schema migration

Flyway creates and evolves schema from:

- `backend/ferko-web-api/src/main/resources/db/migration`

Current key migration groups:

- ToDo task/audit schema
- legacy dataset import schema (`legacy_bootstrap_*`)

## 2) Historical dataset import

Packaged classpath resources:

- `bootstrap/course-isvu-data/*`
- `bootstrap/noviPodatci/*.txt`

are parsed and inserted into DB tables:

- `legacy_bootstrap_course`
- `legacy_bootstrap_enrollment`
- `legacy_bootstrap_schedule`
- `legacy_bootstrap_exam`
- `legacy_bootstrap_raw_line`

This runs automatically when tables are empty.

## 3) Portal enrichment

After DB import, portal state is enriched from dataset snapshot:

- extra courses
- extra student enrollment/group data
- schedule and exam entries
- additional grading/exchange visibility

So browser starts with realistic FERKO-like academic context.

## 4) Runtime controls

Configuration keys:

- `FERKO_BOOTSTRAP_LEGACY_ENABLED`
- `FERKO_PORTAL_BOOTSTRAP_ENABLED`
- `FERKO_PORTAL_BOOTSTRAP_MAX_COURSES`
- `FERKO_PORTAL_BOOTSTRAP_MAX_STUDENTS`
- `FERKO_PORTAL_BOOTSTRAP_MAX_SCHEDULE_ENTRIES`
- `FERKO_PORTAL_BOOTSTRAP_MAX_EXAM_ENTRIES`

In `staging` and `prod`, bootstrap is disabled by default.

## 5) Legacy manual workflows

For historical manual data-loading instructions (old monolith UI), see:

- `docs/legacy/INITIALIZE_ALL_DATA_EN.md`
- `docs/legacy/LOAD_DATA_EN.md`
