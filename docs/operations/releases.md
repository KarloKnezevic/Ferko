# Releases (GHCR container image)

FERKO publishes a versioned, multi-architecture container image to the GitHub Container Registry
(GHCR) on every semantic-version tag. The pipeline is defined in
[`.github/workflows/release-image-ghcr.yml`](../../.github/workflows/release-image-ghcr.yml).

## Cutting a release

A release is triggered purely by pushing a `v<major>.<minor>.<patch>` tag:

```bash
git checkout master && git pull
git tag v1.0.0
git push origin v1.0.0
```

The workflow then:

1. builds the application image from the repository `Dockerfile` for **linux/amd64** and
   **linux/arm64** (via QEMU + Buildx);
2. pushes it to `ghcr.io/<owner>/ferko-web-api` with these tags (from `docker/metadata-action`):
   - the full version, e.g. `1.0.0`
   - the major.minor, e.g. `1.0`
   - the major, e.g. `1`
   - the commit, e.g. `sha-<full-sha>`
3. uploads the resulting tag list and image digest as a build artifact.

No registry credentials are needed beyond the workflow's `GITHUB_TOKEN` (it has `packages: write`).

## Running a published image

```bash
docker pull ghcr.io/<owner>/ferko-web-api:1.0.0

docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e FERKO_DB_URL=jdbc:postgresql://<host>:5432/ferko \
  -e FERKO_DB_USERNAME=ferko -e FERKO_DB_PASSWORD=<secret> \
  ghcr.io/<owner>/ferko-web-api:1.0.0
```

For a self-contained run (application + PostgreSQL) use the production Compose file documented in
[production-deployment.md](./production-deployment.md), pointing `FERKO_IMAGE` at the pulled tag.

## Versioning

Tags are immutable; a new release is a new tag. The `major`/`major.minor` tags are moved to the
newest matching release by `docker/metadata-action`, so downstreams can pin to whichever level of
stability they want (`1`, `1.0`, or `1.0.0`).
