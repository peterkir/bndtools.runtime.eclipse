# GitHub Copilot Instructions — bndtools.runtime.eclipse

This file provides context for GitHub Copilot and other AI coding assistants working in this repository.

---

## Repository Purpose

This workspace provides two small OSGi bundles that enable launching Eclipse-based RCP/IDE applications from a plain OSGi framework container:

- **`bndtools.runtime.applaunch.eclipse3`** — `BundleActivator` for Eclipse 3.x (Equinox `[3.7, 3.9)`)
- **`bndtools.runtime.applaunch.eclipse4`** — `BundleActivator` for Eclipse 4.x (Equinox `[3.10, 4)`)

Both bundles are Java 8 source/target, compiled at JDK 17, and licensed under EPL-1.0.

---

## Build System

- **bnd workspace** managed by the `biz.aQute.bnd.workspace` Gradle plugin (version controlled in `gradle.properties` via `bnd_version`).
- Entry point: `./gradlew build` (build all bundles and tests).
- Global bnd settings: `cnf/build.bnd`.
- Repository and plugin configuration: `cnf/ext/repositories.bnd`.
- The `testOSGi` Gradle task is excluded from CI builds (`-x testOSGi`) because it is known to be flaky in CI environments.

### Key Gradle Tasks

| Task | Effect |
|------|--------|
| `./gradlew build` | Compile, test (minus `testOSGi`), package all bundles |
| `./gradlew :release` | Deploy bundles to all `-releaserepo.*` targets (`cnf/releaserepo/` **and** `dist/bundles/`) |

---

## Repository Plugins (`cnf/ext/repositories.bnd`)

| Name | Type | Notes |
|------|------|-------|
| `Release` | `LocalIndexedRepo` | OSGi OBR index at `cnf/releaserepo/` — primary release store |
| `Local` | `LocalIndexedRepo` | OSGi OBR index at `cnf/localrepo/` — local development |
| `Eclipse Platform` | `MavenBndRepository` | Read-only; resolves Eclipse dependencies from Maven Central using `cnf/ext/eclipse.mvn` |
| `Build` | `FileRepo` | Build-time classpath JARs at `cnf/buildrepo/` |
| `SonatypeRelease` | `MavenBndRepository` | Write-only; outputs Maven directory layout to `dist/bundles/` for Sonatype Central Portal upload |

Both `Release` and `SonatypeRelease` are active release targets:
```bnd
-releaserepo: Release
-releaserepo.sonatype: SonatypeRelease
```

---

## GitHub Actions Workflows

### CI (`.github/workflows/ci.yml`)

- **Triggers:** push or pull request targeting `master`.
- **Steps:** checkout → JDK 17 (Temurin) → `./gradlew build -x testOSGi`.
- No secrets required.

### Release (`.github/workflows/release.yml`)

- **Triggers:**
  - GitHub Release published event (`on.release.types: [published]`).
  - Manual `workflow_dispatch` with `publishing_type` input (`USER_MANAGED` | `AUTOMATIC`).
- **Required secret:** `SONATYPE_BEARER` — Bearer token from [Sonatype Central Portal](https://central.sonatype.com/) → Account → Generate User Token.
- **Steps:**
  1. Build (`./gradlew build -x testOSGi`)
  2. Publish to local Maven layout (`./gradlew :release` → `dist/bundles/`)
  3. Upload to Sonatype Central Portal (`.github/scripts/sonatype-upload.sh dist/bundles`)
  4. Archive `dist/bundles/` as a GitHub Actions artifact (30-day retention)

---

## Sonatype Upload Script (`.github/scripts/sonatype-upload.sh`)

Standalone Bash script; no external dependencies beyond `curl` and JDK `jar`.

| Option | Default | Description |
|--------|---------|-------------|
| `--publishing-type` | `USER_MANAGED` | `USER_MANAGED` = manual release via portal UI; `AUTOMATIC` = auto-publish after validation |
| `--name` | auto-generated | Human-readable deployment name sent to Sonatype |
| `--upload-url` | Sonatype Central Portal URL | Override the upload endpoint |

Environment variable `SONATYPE_BEARER` must be set (error if absent).

The script:
1. Detects the Maven group ID from `.pom` / `maven-metadata.xml` path structure inside `<release-dir>`.
2. Creates a ZIP bundle using `jar cMf` (JDK-native; avoids `zip` tool dependency on Windows/git-bash).
3. POSTs the ZIP to `https://central.sonatype.com/api/v1/publisher/upload`.
4. Writes the returned deployment ID to `<release-dir>_DEPLOYMENTID.txt`.

---

## Conventions

- All bnd `Bundle-Version` values use a timestamp qualifier: `0.1.0.${tstamp;yyyyMMdd-HHmmss}`.
- Java source/target: **1.8** (enforced in `cnf/build.bnd` via `javac.source` / `javac.target`).
- Git metadata is embedded in bundle manifests via `Git-Descriptor` and `Git-SHA` headers.
- Do **not** add new Maven/Gradle dependencies without checking against existing `cnf/ext/*.mvn` / `cnf/ext/*.bnd` entries first.
- Do **not** add a `sonatype-status.sh` script — only uploading is required by this project.
- The `SONATYPE_BEARER` secret must never be committed to source files; it is consumed exclusively via `${{ secrets.SONATYPE_BEARER }}` in workflows and the `SONATYPE_BEARER` environment variable in the upload script.
