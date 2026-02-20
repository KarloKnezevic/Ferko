# Legacy Full Data Initialization (English)

Source translated from `docs/legacy/source/InicijalizacijaSvihPodataka.txt`.

This applies only to the legacy monolith.

## Hibernate behavior notes

In legacy `persistence.xml`:

- `hibernate.show_sql=true/false`
- `hibernate.format_sql=true/false`
- `hibernate.hbm2ddl.auto` can be `create`, `create-drop`, `update`

`create` may fail if old tables/data remain due to FK constraints.
`update` usually works, but not always.

## Full initial data load approach

For complete initialization (including users and enrollment placement), run the legacy test `BasicDBTesting` from Eclipse and ensure it points to MySQL persistence unit:

```java
// emf = Persistence.createEntityManagerFactory("jcmstestdb");
emf = Persistence.createEntityManagerFactory("jcmsdb");
```

This is equivalent to `Prepare.action` plus additional user/enrollment loading.

## Legacy login note

A documented test student password pattern is:

- `AAAAAAAAAAAA` (12 uppercase `A` characters)

## Legacy file logging note

Legacy app can log to:

- `jcms_output.log` in current directory

See example logger usage in `SynchronizerService` (`debug`, `trace`, `info`).
