# Legacy Data Loading Workflow (English)

Source translated from `docs/legacy/source/UcitavanjePodataka.txt`.

This applies only to the legacy monolith UI.

## 0) Install first

Follow:

- `docs/legacy/HOW_TO_INSTALL_EN.md`

## 1) Create academic year and semester

Legacy UI action: `Create new semester`.

Example values:

- Id: `2008L`
- Academic year: `2008/2009`
- Semester: `summer`
- Start: `2009-02-23 00:00:00`
- End: `2009-08-31 23:59:59`

## 2) Load students and course enrollments (ISVU)

Legacy UI action: `Synchronization of students on courses`.

Paste import data from:

- `various-files/noviPodatci/isvuUTF8.txt`

First column is student JMBAG (10-digit unique numeric identifier).

## 3) Load timetable

Legacy UI action: `Sync timetable`.

Example file:

- `various-files/noviPodatci/satnica.txt`

## 4) Load exam schedule

Legacy UI action: `Enter exam schedule`.

Example file:

- `various-files/noviPodatci/raspored-final-mi1.txt`

## 5) Load lab schedule

Legacy UI action: `Sync lab schedule`.

Example file:

- `various-files/noviPodatci/rasporedC03_tl.txt`
