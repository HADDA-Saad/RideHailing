# SonarQube Cloud setup

This repository uses one SonarQube Cloud project for the Java backend and the
React/TypeScript frontend. Sonar hosts the dashboard; no local SonarQube server,
Docker service, or Sonar database is needed.

## What each tool does

| Tool | Responsibility |
| --- | --- |
| JUnit / Vitest | Run the tests and fail if an assertion fails. |
| JaCoCo | Measure which Java code runs during the backend tests. |
| Vitest V8 coverage provider | Measure which frontend code runs during the tests. |
| SonarScanner | Analyze source code and upload results and coverage to Cloud. |
| SonarQube Cloud | Display findings and evaluate the quality gate. |

Tests can pass while leaving entire features untested. Coverage reveals those
gaps; it does not prove that the tests make useful assertions. SonarQube can
analyze source code without coverage, but coverage is included in this setup.

## How the workflow fits together

```text
Backend Build and Test                  Frontend Build and Test
  Maven verify                           Vitest with coverage + build
  -> compiled Java classes               -> coverage/lcov.info
  -> JaCoCo XML                          |
  -> dependency JARs                     |
               \                        /
                SonarQube Quality Gate
                -> analyze both applications
                -> upload to SonarQube Cloud
                -> wait for gate result
                -> fail the job if the gate fails
```

GitHub artifacts transfer the generated files between jobs. They are temporary
build outputs with one-day retention, not files committed to Git. Tests run once
per application; the analysis job reuses their outputs.

The existing backend and frontend check names are preserved for branch rules.
Docker image jobs continue to depend on the test/build jobs, as before. Requiring
the Sonar check in branch rules controls merging; this setup does not add a
Sonar dependency to Docker publishing.

## 1. Create and connect the Cloud project

1. Sign in to [SonarQube Cloud](https://sonarcloud.io) using GitHub.
2. Import your GitHub organization/account and allow the SonarQube Cloud GitHub
   app access to the RideHailing repository.
3. Import RideHailing as **one project**, covering both applications. Choose a
   plan that supports the repository visibility and PR analysis you need; check
   current plan eligibility before choosing a paid option.
4. Choose **GitHub Actions / CI-based analysis**. If automatic analysis is
   enabled, disable it under the project's Administration > Analysis Method.
   The workflow supplies the test coverage and analysis instead.
5. Copy the exact **organization key** and **project key** from Cloud. These are
   identifiers, not necessarily the display names or your GitHub username.
6. Create an analysis token with permission to analyze this project.

Use the host/region selected during signup. The workflow defaults to the EU
service at `https://sonarcloud.io`. Set `SONAR_HOST_URL` to the URL provided by
your Cloud setup if different (for example, the US service).

## 2. Configure GitHub Actions

In the GitHub repository, open **Settings > Secrets and variables > Actions**.

Under **Secrets**, create:

| Repository secret | Value |
| --- | --- |
| `SONAR_TOKEN` | The analysis token generated in Cloud. |

Under **Variables**, create:

| Repository variable | Value |
| --- | --- |
| `SONAR_ORGANIZATION` | Exact organization key from Cloud. |
| `SONAR_PROJECT_KEY` | Exact project key from Cloud. |
| `SONAR_HOST_URL` | Optional; defaults to `https://sonarcloud.io`. |
| `SONAR_ENABLED` | `true`, after the preceding values are configured. |

Never paste the token into a source file or a pull request. It belongs in GitHub
Secrets. The workflow only checks that the token is present; it does not print it.

Without `SONAR_ENABLED=true`, tests and builds still run with coverage, but the
Sonar analysis job is skipped. This allows account setup before activation.

## 3. Run and verify the first analysis

Commit the setup on a feature branch and open a PR into `main`. Alternatively,
use Actions > RideHailing CI > Run workflow on a branch containing this setup.
Check that:

- The backend and frontend jobs pass.
- The **SonarQube Quality Gate** job runs rather than being skipped.
- The scanner imports both JaCoCo XML and frontend LCOV coverage.
- Both Java and TypeScript files appear in the Cloud project.
- The scanner links to the expected project and PR, and reports a gate result.

Review the first findings in Cloud. The configured quality gate can fail on new
code coverage or detected issues even when every test passes. That is a quality
result, not necessarily an integration failure.

## 4. Require the quality gate before merging

After confirming the scan works, add **SonarQube Quality Gate** as a required
status check in the active ruleset targeting `main`, alongside the existing
backend and frontend checks. Keep the pull request requirement enabled and review
bypass permissions.

A workflow failure blocks merging only when GitHub rules require that check.
The scanner uses `sonar.qualitygate.wait=true`, so an uploaded scan alone is not
treated as a passing quality gate.

### Fork and Dependabot pull requests

Tests still run for these PRs, but the Sonar job is skipped because those runs
do not receive the Cloud token. GitHub treats a skipped job as successful for
required checks, so this does **not** enforce Sonar analysis for these PRs.
Review them and run analysis on a trusted branch before merging if needed.
Do not expose the token by switching to `pull_request_target` and executing
untrusted PR code.

## Read the configuration

| File | What to look for |
| --- | --- |
| `backend/pom.xml` | JaCoCo records coverage and reports at `verify`; the dependency plugin pins its version and the `sonar-libraries` output directory. |
| `frontend/package.json` | `test:coverage` runs the existing tests with coverage enabled. |
| `frontend/vite.config.ts` | V8 provider, source inclusion, and text/HTML/LCOV reports. |
| `sonar-project.properties` | Analysis paths, Java binaries, dependency JARs, and coverage report paths. |
| `.github/workflows/ci.yml` | Run tests, transfer outputs, analyze, and wait for the gate. |

Only application source directories are scanned. Tests are classified separately;
frontend test files and TypeScript declaration files are excluded from production
source. Application files with no tests remain included in coverage.

## Run coverage locally

Backend, from `backend/` on Windows:

```powershell
.\mvnw.cmd clean verify
```

Open `backend/target/site/jacoco/index.html` in a browser. The scanner imports
`backend/target/site/jacoco/jacoco.xml`.

Frontend, from `frontend/`:

```powershell
npm ci
npm run test:coverage
```

Open `frontend/coverage/index.html` in a browser. The scanner imports
`frontend/coverage/lcov.info`.

Coverage reports and scanner working files are ignored by Git. Local coverage
commands do not upload code to Cloud; uploading occurs in the CI scanner step.

## References

- [Cloud setup with GitHub](https://docs.sonarsource.com/sonarqube-cloud/getting-started/github)
- [Java coverage and JaCoCo](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/test-coverage/java-test-coverage)
- [JavaScript/TypeScript coverage and LCOV](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/test-coverage/javascript-typescript-test-coverage)
