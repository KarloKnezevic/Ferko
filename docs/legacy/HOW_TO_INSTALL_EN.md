# Legacy Install Guide (English)

Source translated from `docs/legacy/source/HOW-TO-INSTALL.txt`.

This guide is for the legacy Ferko/JCMS monolith (Ant + Struts + Tomcat).

## 1) Create legacy MySQL database and users

```sql
create database jcmsdb default character set 'utf8' default collate 'utf8_bin';
grant all on jcmsdb.* to 'jcms_user'@'localhost' identified by 'tajna3';
grant all on jcmsdb.* to 'jcms_user'@'%' identified by 'tajna3';
flush privileges;
```

## 2) Certificates (FERWeb authorization)

Legacy trust store setup example:

```text
-Djavax.net.ssl.trustStore=C:\eclipse_workspaces\ws3\jcms\certifikati\keystore
```

## 3) MySQL UTF-8 configuration (legacy requirement)

In `my.ini`:

`[mysqld]`

- `default-character-set=utf8`
- `default-collation=utf8_bin`
- `character-set-server=utf8`
- `collation-server=utf8_bin`

`[client]`

- `default-character-set=utf8`

Restart MySQL.

## 4) Build and packaging

1. Create `allConfigs/`.
2. Copy `configuration-sample.properties` to `allConfigs/configuration.properties`.
3. Configure parameters in `allConfigs/configuration.properties`.
4. Run:

```bash
ant local
ant package
ant war
```

The produced WAR is deployed to Tomcat.

## 5) Prepare database from web endpoint

Open:

- `http://localhost:8080/ferko/Prepare.action`

For full details, see `docs/legacy/INITIALIZE_ALL_DATA_EN.md`.

## Legacy room reservation subsystem (summary)

Reservation providers were historically configured in
`configs/classes/reservation-managers.properties`.

Modernized repository cleanup removed obsolete binary reservation snapshots that were tied to
legacy runtime internals (`file-reservations.bin`). For current local/demo use, rely on the
modern Spring Boot + PostgreSQL flow described in `docs/getting-started/`.
