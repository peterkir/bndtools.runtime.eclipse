# bndtools.runtime.eclipse

OSGi bundles that enable launching Eclipse-based applications from a standard OSGi framework.

Two bundles are provided:

| Bundle | Description |
|--------|-------------|
| `bndtools.runtime.applaunch.eclipse3` | Activator for Eclipse 3.x (Equinox `[3.7, 3.9)`) |
| `bndtools.runtime.applaunch.eclipse4` | Activator for Eclipse 4.x (Equinox `[3.10, 4)`) |

Both bundles are compiled against Java 8 source/target and licensed under [EPL-1.0](http://www.opensource.org/licenses/eclipse-1.0.php).

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17 (build) / targets Java 8 bytecode |
| Gradle | bundled via `gradlew` |

---

## Building

```bash
./gradlew build
```

This compiles all bundles, runs unit tests (excluding the flaky `testOSGi` task), and places the built JARs in each project's `generated/` directory.

---

## Project Layout

```
bndtools.runtime.eclipse/
├── bndtools.runtime.applaunch.eclipse3/   Eclipse 3.x launch activator
├── bndtools.runtime.applaunch.eclipse3.test/
├── bndtools.runtime.applaunch.eclipse4/   Eclipse 4.x launch activator
├── bndtools.runtime.applaunch.eclipse4.test/
├── cnf/                                   bnd workspace configuration
│   ├── build.bnd                          global build settings
│   ├── ext/
│   │   ├── repositories.bnd               repository plugin configuration
│   │   └── eclipse.mvn                    Eclipse Platform Maven index
│   └── releaserepo/                       local OSGi release repository
├── .github/
│   ├── scripts/
│   │   └── sonatype-upload.sh             Sonatype Central Portal upload script
│   └── workflows/
│       ├── ci.yml                         CI workflow (push / PR on master)
│       └── release.yml                    Release workflow (publish to Maven Central)
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## Continuous Integration

The **CI** workflow (`.github/workflows/ci.yml`) runs automatically on every push to `master` and on pull requests targeting `master`.

Steps:
1. Check out the repository.
2. Set up JDK 17 (Temurin).
3. `./gradlew build -x testOSGi`

---

## Releasing to Maven Central (Sonatype Central Portal)

The **Release** workflow (`.github/workflows/release.yml`) publishes the built OSGi bundles to [Maven Central](https://central.sonatype.com/) via the Sonatype Central Portal.

### Trigger

| Event | How |
|-------|-----|
| **Automatic** | Publish a GitHub Release (Actions → Releases → Publish release) |
| **Manual** | Actions → Release → Run workflow, choose `USER_MANAGED` or `AUTOMATIC` |

### How it works

1. **Build** — `./gradlew build -x testOSGi`
2. **Publish to local Maven layout** — `./gradlew :release` deploys all bundles to `dist/bundles/` in standard Maven directory layout using the `SonatypeRelease` repository configured in `cnf/ext/repositories.bnd`.
3. **Upload to Sonatype** — `.github/scripts/sonatype-upload.sh` zips `dist/bundles/` and POSTs it to the Sonatype Central Portal upload API.
4. **Archive artifact** — `dist/bundles/` is uploaded as a GitHub Actions artifact (retained 30 days).

### Publishing types

| Type | Behaviour |
|------|-----------|
| `USER_MANAGED` (default) | Upload passes validation, then must be manually released at <https://central.sonatype.com/publishing> |
| `AUTOMATIC` | Upload is automatically published to Maven Central after passing validation |

### Required secret

Add a repository secret named **`SONATYPE_BEARER`** (Settings → Secrets and variables → Actions → New repository secret).

Obtain the token from [Sonatype Central Portal](https://central.sonatype.com/) → **Account → Generate User Token**.

### Sonatype upload script

`.github/scripts/sonatype-upload.sh` is a standalone Bash script used by the release workflow. It can also be run locally:

```bash
SONATYPE_BEARER=<token> \
  ./.github/scripts/sonatype-upload.sh [--publishing-type AUTOMATIC] dist/bundles
```

The deployment ID returned by the API is written to `dist/bundles_DEPLOYMENTID.txt`.

---

## Repository Configuration (bnd)

`cnf/ext/repositories.bnd` defines the following bnd repository plugins:

| Name | Type | Purpose |
|------|------|---------|
| `Release` | `LocalIndexedRepo` | Local OSGi release repository (`cnf/releaserepo/`) |
| `Local` | `LocalIndexedRepo` | Local development repository (`cnf/localrepo/`) |
| `Eclipse Platform` | `MavenBndRepository` | Eclipse dependencies from Maven Central |
| `Build` | `FileRepo` | Build-time classpath JARs (`cnf/buildrepo/`) |
| `SonatypeRelease` | `MavenBndRepository` | Write-only Maven layout output for Sonatype upload (`dist/bundles/`) |

`-releaserepo: Release` and `-releaserepo.sonatype: SonatypeRelease` mean that `./gradlew :release` deploys to both repositories simultaneously.
