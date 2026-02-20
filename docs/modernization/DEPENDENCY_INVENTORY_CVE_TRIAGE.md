# Dependency Inventory and CVE Triage Baseline

Date: 2026-02-19
Scope: Phase 1 (Build and Dependency Migration)

## 1. Objective
Provide a repeatable dependency inventory and CVE triage baseline for the Ferko modernization track.

## 2. Inventory Sources

Legacy source:
- `/Users/karloknezevic/Desktop/Ferko/lib` (vendored jars from legacy Ant build)

Modern source:
- Maven dependency trees generated from the multi-module build
- CycloneDX SBOM generated from the Maven reactor

Automation entrypoint:
- `/Users/karloknezevic/Desktop/Ferko/scripts/generate_dependency_inventory.sh`
- `/Users/karloknezevic/Desktop/Ferko/scripts/generate_dependency_triage_summary.py`

Generated artifacts:
- `target/dependency-inventory/ferko-sbom.json`
- `target/dependency-inventory/trees/*.txt`
- `target/dependency-inventory/metadata.txt`
- `target/dependency-vulnerability/triage-summary.md` (generated when vulnerability scan job runs)

## 3. Legacy Dependency Snapshot

Observed on 2026-02-19:
- Vendored jars in `lib/`: 63

High-risk legacy framework set identified:
- Struts 2.0.11.x (`struts2-core-2.0.11.2.jar`, `struts2-tiles-plugin-2.0.11.1.jar`)
- XWork 2.0.5 (`xwork-2.0.5.jar`)
- Hibernate 3.x (`hibernate3.jar`, related hibernate-* jars)
- Log4j 1.2.15 (`log4j-1.2.15.jar`)
- MySQL Connector 5.1.x (`mysql-connector-java-5.1.26-bin.jar`)
- OGNL 2.6.x (`ognl-2.6.11.jar`)
- Commons HttpClient 3.0.1 (`commons-httpclient-3.0.1.jar`)

Triage status for this set:
- Decision: `REPLACE/REMOVE` in modernization track
- Rationale: obsolete ecosystem, known security exposure patterns, incompatible with Java 21 modernization goals

## 4. Modern Maven Dependency Snapshot

Current direct module-level dependencies:
- `ferko-domain`: JUnit 5 (test)
- `ferko-application`: `ferko-domain`, JUnit 5 (test)
- `ferko-infrastructure`: `ferko-application`, JUnit 5 (test)
- `ferko-security`: `ferko-application`, JUnit 5 (test)
- `ferko-web-api`: `ferko-application`, `ferko-infrastructure`, `ferko-security`, Spring Boot Web, Spring Boot Actuator, Spring Boot Test

Current transitive runtime foundation is Spring Boot 3.3.x stack (Tomcat 10.1.x, Spring Framework 6.1.x, SLF4J/Logback).

## 5. CVE Scan Baseline and Blockers

Local scan attempt on 2026-02-19 without NVD API key:
- Command reached OWASP Dependency-Check but NVD update returned 403/404
- Result: no reliable vulnerability dataset refresh; scan cannot be treated as authoritative

Implication:
- Reliable CVE triage requires an `NVD_API_KEY` in CI secrets

## 6. CI Integration

Dependency governance is now integrated into CI:
- Quality gate (`./mvnw verify`) includes Spotless, Checkstyle, JaCoCo, Maven Enforcer
- Dependency inventory job publishes dependency tree + SBOM artifacts
- Vulnerability scan job runs OWASP Dependency-Check:
  - strict mode when `NVD_API_KEY` is configured
  - non-blocking fallback with warning when key is missing
  - always publishes `triage-summary.md`
- PR dependency delta scan via GitHub Dependency Review action

CI workflow file:
- `/Users/karloknezevic/Desktop/Ferko/.github/workflows/maven-phase1.yml`

## 7. Immediate Next Actions

1. Configure repository secret `NVD_API_KEY` to enable authoritative CVE scanning.
2. Start `dependency-check` suppression file only for confirmed false positives.
3. Maintain tracked triage log per release in:
   - `/Users/karloknezevic/Desktop/Ferko/docs/modernization/DEPENDENCY_VULNERABILITY_TRIAGE_LOG.md`
4. For each legacy jar family listed above, create migration tickets with target replacement and removal deadline.

## 8. Reproduction Commands

Generate inventory artifacts:
```bash
./scripts/generate_dependency_inventory.sh
```

Run vulnerability scan with strict CVSS gate (requires API key):
```bash
NVD_API_KEY=... ./mvnw -B -ntp -DskipTests -Dformats=HTML,JSON -DfailBuildOnCVSS=9 -DnvdApiKeyEnvironmentVariable=NVD_API_KEY -DsuppressionFile=build-tools/dependency-check/suppressions.xml org.owasp:dependency-check-maven:9.2.0:aggregate
```
