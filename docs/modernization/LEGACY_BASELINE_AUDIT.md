# Ferko Legacy Baseline Audit

## Scope
This baseline is a repository-grounded assessment of the current Ferko (formerly JCMS) codebase in `/Users/karloknezevic/Desktop/Ferko`.

Audit date: 2026-02-19

## Current State Snapshot

| Area | Observed state |
|---|---|
| Build tool | Apache Ant (`/Users/karloknezevic/Desktop/Ferko/build.xml`) |
| Web runtime model | External WAR deployment to Tomcat, Servlet 2.4 descriptor (`/Users/karloknezevic/Desktop/Ferko/configs/WEB-INF/web.xml`) |
| MVC/web framework | Struts 2 + Tiles (`struts.xml`, JSP taglibs, Struts filter dispatcher) |
| ORM/persistence | Hibernate 3 / JPA-era XML config (`/Users/karloknezevic/Desktop/Ferko/configs/classes/META-INF/persistence.xml`) |
| Database driver | MySQL connector 5.1.x jars in repo |
| Logging | Log4j 1.2.x |
| Packaging style | Monolithic project with mixed concerns (web, auth, desktop tools, tests, applets/planning visualizers) |
| Configuration | Tokenized property substitution into XML/properties via Ant replace tasks |
| Documentation language | Mixed Croatian/English; major guides in PDF |

## Size and Coupling Indicators

| Metric | Value |
|---|---|
| Total Java files under `src/` | 1227 |
| Java files under core `src/java` | 1169 |
| Desktop Java utilities | 31 |
| Test source files in `src/tests` | 16 |
| JSP files under `web/WEB-INF/pages` | 325 |
| Web assets/files under `web/` | 681 |
| Config files under `configs/` | 82 |
| Domain model classes under `src/java/hr/fer/zemris/jcms/model` | 125 |
| Struts action classes under `src/java/hr/fer/zemris/jcms/web/actions` | 273 |

Interpretation: this is a large, tightly coupled monolith with heavy server-side rendering and action-layer density.

## Security and Authentication Baseline

Observed mechanisms:
- JAAS/Tomcat Realm login module: `/Users/karloknezevic/Desktop/Ferko/src/java/hr/fer/zemris/jcms/jaas/module/JCMSLoginModule.java`
- POP3 authenticator: `/Users/karloknezevic/Desktop/Ferko/src/auth/hr/fer/zemris/auth/pop3auth/Pop3Authenticator.java`
- LDAP authenticator: `/Users/karloknezevic/Desktop/Ferko/src/auth/hr/fer/zemris/auth/ldapauth/LdapAuthenticator.java`
- FERWeb XML-RPC authenticator: `/Users/karloknezevic/Desktop/Ferko/src/auth/hr/fer/zemris/auth/ferwebauth/FerWebAuthenticator.java`
- Custom SSO checker integration: `/Users/karloknezevic/Desktop/Ferko/src/java/hr/fer/zemris/jcms/service/SSOService.java`

Primary risks:
- Legacy auth protocols and custom auth flows increase attack surface.
- No modern centralized policy engine (OIDC/JWT lifecycle, token revocation strategy, adaptive auth).
- Log4j 1.x and old framework stack imply known vulnerability exposure and limited patch path.

## Persistence and Data Baseline

Observed persistence configuration:
- Hibernate dialect configured for MySQL InnoDB in sample config.
- Runtime JDBC URL uses manual UTF-8 settings.
- Ant token replacement injects DB credentials and Hibernate settings into generated configs.

Primary risks:
- Configuration drift between environments.
- Non-versioned schema evolution behavior (`hbm2ddl` usage patterns).
- Weak reproducibility due mutable runtime config generation.

## Build and Deployment Baseline

Current build pipeline behavior in `build.xml`:
- Compiles multiple source roots (`src/auth`, `src/java`, `src/desktop`, `src/tests`, `src/planning`, `src/occvisualizer`, `src/sscoretree`).
- Packages multiple jars plus web artifacts and applet-related deliverables.
- Performs environment-specific file filtering and token substitution.

Primary risks:
- Ant-centric build logic is difficult to evolve into CI/CD quality gates.
- Dependency versions are vendored as local jars, preventing reliable software bill-of-materials and automated updates.
- Deployment reproducibility depends on external mutable state (`CATALINA_HOME`, generated prod directories).

## Documentation Baseline

Existing docs include:
- Legacy README (Croatian-heavy): `/Users/karloknezevic/Desktop/Ferko/README.md`
- Installation notes in text files and PDF admin/user guides.

Primary risks:
- Incomplete English operational knowledge transfer.
- Onboarding and institutional continuity risk.

## Migration Constraints and Non-Negotiables

1. Big-bang rewrite is high risk for a system of this size/coupling.
2. A strangler migration with production-safe coexistence is required.
3. Build modernization must happen before broad feature refactors.
4. Security uplift must occur early (auth + dependency upgrades), not at the end.
5. Database migration must be scripted, versioned, and reversible.

## Recommended Immediate Actions (Phase 0)

1. Freeze risky functional changes on legacy trunk except critical fixes.
2. Establish modernization branch strategy and ADR process.
3. Define source-of-truth architecture docs in English.
4. Introduce CI baseline (build + static checks + test execution visibility).
5. Start API/domain boundary extraction for high-value modules first.

