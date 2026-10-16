# Hintmatrix Locust implementation and testing guide

## 1. What this setup does

Locust runs on your computer and sends HTTP requests to the deployed Spring Boot backend:

```text
PowerShell → local Locust process → https://projects-v8fc.onrender.com
                                      ↓
                                backend database
```

The React frontend at `https://hintmatrix-project.onrender.com` is not part of this test.
You do not need to open the frontend or deploy these Python files to Render.
This measures backend API behavior, not browser rendering or page-load performance.

## 2. Files and their purposes

| File or folder | Purpose | Commit to Git? |
| --- | --- | --- |
| `locustfile.py` | Virtual student, login, workflows, response checks and task weights | Yes |
| `requirements.txt` | Pinned Locust and python-dotenv dependencies | Yes |
| `.env.example` | Configuration template without credentials | Yes |
| `.env` | Your local target, credentials and runtime settings | No |
| `.gitignore` | Excludes credentials, virtual environment, account CSVs and results | Yes |
| `README.md` | API contracts, workload, commands and load-test prerequisites | Yes |
| `IMPLEMENTATION_GUIDE.md` | This implementation walkthrough and practical test guide | Yes |
| `.venv/` | Local Python environment created during setup | No |
| `accounts.csv` | Optional dedicated student credentials for multiple users | No |
| `results/` | Generated HTML and CSV reports | No |

## 3. How the implementation works

The implementation was based on the repository's controllers, services, DTOs, Spring Security
configuration and JWT filter. It uses existing APIs and makes no backend business-logic, schema,
frontend or deployment changes.

### Configuration and startup

`load_dotenv()` reads `.env` next to `locustfile.py`. Existing process environment variables take
precedence. `validate_host()` checks that the backend is an HTTP(S) origin and rejects the known
frontend hostname. `prepare_accounts()` runs when testing starts, reads credentials, checks the
requested headless user count and creates a local queue of accounts. Distributed master/worker
execution is rejected because account allocation is local to one process.

`read_accounts()` uses `ACCOUNTS_CSV` when configured; otherwise it uses `TEST_EMAIL` and
`TEST_PASSWORD`. It rejects missing credentials and duplicate CSV emails. It does not provision users.

### Student lifecycle and authentication

`StudentUser` extends Locust's `HttpUser`. One virtual user represents one student making requests
sequentially, with think time. `on_start()` reserves an account and calls `login()`:

```text
POST /api/auth/login
JSON fields: email, password
Expected: HTTP 200, role STUDENT, tokenType Bearer, nonempty token, userId
```

The token is kept in memory and sent in the client's `Authorization: Bearer ...` header.
There is no hardcoded token and no session-cookie authentication flow. Failed login stops that
virtual user. `on_stop()` returns a reserved account to the queue when account sharing is disabled.
Login updates the account's last-login timestamp and generates backend audit logs.

### Study workflow: `study()`

`@task(3)` gives this journey three times the selection weight of the exam journey:

1. GET `/api/course`, then randomly select a returned course.
2. GET `/api/subjects`, then select a subject whose `courseId` matches.
3. GET `/api/chapter`, then select a chapter whose course and subject match.
4. GET `/api/topics`, then select a topic whose course and chapter match.
5. GET `/api/questions/filter` with the selected `courseId`, `chapterId` and `topicId`.
6. If questions exist, GET `/api/questions/{questionId}` using a returned ID.

Catalogue APIs return full lists. Related entries are selected locally; the test does not invent
subject/chapter/topic filtering endpoints. A random incomplete branch can end the journey early.
Think time is inserted between reads.

### Exam workflow: `exams()`

`@task(1)` performs:

1. GET `/api/exams`.
2. Choose an exam from that student's returned list.
3. GET `/api/exams/{examId}`.
4. GET `/api/exams/{examId}/questions`.

Student exam listings depend on section assignment and exclude attempted exams in the inspected
service implementation. The test does not submit an exam or create an attempt.
Task weighting is probabilistic; a short smoke run may not select every journey.

### Validation and failure handling

`get_data()` requires HTTP 200, JSON of the expected list/object shape and positive integer IDs.
Detail IDs must match the requested ID. Filtered questions must match all three mapping IDs.
It groups paths containing numeric IDs under stable report names such as `/api/exams/{id}/questions`.

Redirects are not followed. Unexpected status codes and JSON contracts are failures.
A protected request returning 401 remains recorded as failed, then the user logs in again;
the original request is not retried. If re-login fails, the user stops.

`coverage_failure()` adds a synthetic `COVERAGE` event for missing related catalogue data.
These have zero response time and are not HTTP requests. Exclude them when assessing HTTP latency
and throughput. Empty API lists fail validation when `REQUIRE_DATA=true`.

## 4. Configuration reference

| Setting | Meaning |
| --- | --- |
| `TARGET_HOST` | Backend origin; defaults to the deployed backend |
| `FRONTEND_URL` | Informational only; the Python test never uses it |
| `TEST_EMAIL`, `TEST_PASSWORD` | One existing dedicated student for smoke |
| `ACCOUNTS_CSV` | Optional CSV path, relative to this folder or absolute |
| `ALLOW_SHARED_ACCOUNT` | Defaults false; keep false for representative multi-user runs |
| `REQUEST_TIMEOUT` | Timeout per Locust HTTP request, in seconds; default 30 |
| `THINK_MIN`, `THINK_MAX` | Minimum/maximum simulated reading pause in seconds; default 2–5 |
| `REQUIRE_DATA` | Default true; fail for empty required content or missing hierarchy links |

`REQUIRE_DATA=false` permits empty lists and skips incomplete branches. It does not bypass login,
HTTP status or malformed JSON checks. A run with skipped branches cannot demonstrate full coverage.

## 5. First-time setup in Windows PowerShell

Open PowerShell or an IDE terminal configured for PowerShell. This same window is the “Locust terminal.”

```powershell
Set-Location C:\Users\narsa\intellijWorkspace3\ProjectS\performance-tests
py -3 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
Copy-Item .env.example .env
notepad .env
New-Item -ItemType Directory -Force results | Out-Null
```

Enter dedicated STUDENT credentials in `.env`, save and close Notepad. Do not paste passwords or tokens
into reports or chat. If `.env` already contains your settings, skip `Copy-Item`: copying again
overwrites those settings. If `.venv` already exists and dependencies are installed, skip their creation.
If `py` is not recognized, install Python 3.11+ and reopen PowerShell.

## 6. Wake the backend before measurement

```powershell
curl.exe --silent --show-error --max-time 180 --output NUL --write-out 'status=%{http_code} total_seconds=%{time_total}\n' https://projects-v8fc.onrender.com/api/auth/me | Tee-Object results\wake-up.txt
```

Expected status: **401**, because this single request has no token. It confirms an HTTP response from
the backend endpoint, not successful authentication or database readiness. A fast response may mean
the service was already awake; it is not automatically a cold-start measurement. Record idle state
and time. Investigate unexpected status or timeout before starting Locust.

## 7. Run the one-user smoke test

```powershell
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 1 -r 1 -t 2m --csv results\smoke --csv-full-history --html results\smoke.html --exit-code-on-error 1
# Check immediately after Locust, before running another native command:
$LASTEXITCODE
Start-Process .\results\smoke.html
```

Wait for the command to finish. `0` means no recorded failures; `1` indicates failures with this
command. Startup/configuration errors also require investigation regardless of exit code.
Use a new report prefix for subsequent runs to preserve earlier results.

| Command option | Meaning |
| --- | --- |
| `-f locustfile.py` | File containing the user simulation |
| `--headless` | Run directly in the terminal without the browser control UI |
| `-u 1` | Target number of concurrent virtual users |
| `-r 1` | Spawn one virtual user per second |
| `-t 2m` | Total run duration, including ramp |
| `--csv results\smoke` | Prefix for generated CSV files |
| `--csv-full-history` | Include per-endpoint history |
| `--html results\smoke.html` | Save an HTML report |
| `--exit-code-on-error 1` | Return a failure exit code if requests failed |

Confirm all intended routes appear in the report. Extend the one-user duration if random task
selection did not cover exams. Do not increase load to solve missing data.

## 8. How to confirm requests reached the application

Watch the endpoint request counts increase in the same PowerShell window. Successful login and
protected reads should have HTTP 200. Open the Render backend service logs during the run and
match timestamps with controller messages such as `Login attempt received`,
`Received request to fetch all courses.`, `Fetching all subjects`, and `Fetching all exams for user=`.
Log visibility depends on deployed version and logging configuration.

Successful HTTP rows plus matching backend logs provide stronger evidence than request counts alone:
counts can also increase for connection failures. Nothing needs to appear in the React browser UI.

## 9. Reports and interpretation

| Output | Use |
| --- | --- |
| `smoke.html` | Open in a browser for readable summary statistics |
| `smoke_stats.csv` | Aggregated per-endpoint counts, times, rates and percentiles |
| `smoke_stats_history.csv` | Timestamped statistics and user-count trends |
| `smoke_failures.csv` | Grouped failures and occurrence counts |
| `smoke_exceptions.csv` | Locust execution exceptions; may contain only headers when none occur |
| `wake-up.txt` | Separate unauthenticated wake-up observation |

Response times are milliseconds in Locust statistics. RPS means requests per second, not students
per second. p95 means approximately 95% of observed responses were at or below that duration.
Separate login, catalogue, question and exam routes when evaluating results. Synthetic `COVERAGE`
events affect aggregate numbers; inspect HTTP rows independently.

Full-run reports include ramp and initial login. Evaluate sustained load only after the target user
count stabilizes. History percentiles are rolling/cumulative Locust statistics, not exact percentiles
recomputed for any arbitrary selected time window. Check actual achieved users, generator resources,
Render resources and database metrics before drawing conclusions.

## 10. Troubleshooting

| Symptom | Meaning and next step |
| --- | --- |
| `COVERAGE course has no related subjects` | Course and subject requests succeeded, but the randomly selected course had no matching subject. Review HTTP rows and related data. |
| `subject has no related chapters` / `chapter has no related topics` | Selected catalogue branch is incomplete. Prepare complete representative content. |
| `JSON contract mismatch or required data empty` | Inspect endpoint and backend logs: required list may be empty, ID/mapping invalid, or deployed response different. |
| `Login HTTP 401` | Check dedicated credentials, active account and deployed authentication behavior. |
| Login contract mismatch | Account may not have STUDENT role or login response differs from inspected code. |
| Protected read 401 / 403 | Authentication or authorization failed; check account and backend logs. |
| 429 | Service is limiting requests. Stop escalation and review approved limits. |
| 5xx or timeout | Correlate server/database logs, resource usage and cold-start state. It is not automatically proof of overload. |
| Insufficient dedicated accounts | Provide enough unique CSV accounts for requested headless users. |
| Python executable not found | Create `.venv` and confirm you are in `performance-tests`. |
| No module named locust | Install `requirements.txt` using the same `.venv` Python executable. |

For the missing-subject failure you encountered, `REQUIRE_DATA=false` is suitable for a limited
connectivity check when incomplete content is intentional. Retain strict mode and prepare related
content when the goal is full student-workflow coverage. This implementation does not currently
provide a course-ID allowlist, so it can randomly choose any course returned by the API.

## 11. Optional browser dashboard

```powershell
.\.venv\Scripts\python.exe -m locust -f locustfile.py --host https://projects-v8fc.onrender.com
```

Open `http://localhost:8089` in your browser. This address is your local Locust dashboard; the target
remains the deployed backend. Start with one user and spawn rate one. Ensure enough account rows
before changing user counts in the UI. Use Ctrl+C in PowerShell to terminate the local Locust process.

## 12. Preparing multi-user testing

Use existing dedicated student accounts. Set these local `.env` values:

```dotenv
ACCOUNTS_CSV=accounts.csv
ALLOW_SHARED_ACCOUNT=false
REQUIRE_DATA=true
```

The CSV must have exactly the intended credential columns, with one unique student per row:

```csv
email,password
student-load-001@example.invalid,REPLACE_LOCALLY
student-load-002@example.invalid,REPLACE_LOCALLY
```

Those sample accounts are placeholders, not usable accounts. Use CSV quoting for commas or quotes.
Keep the file private. Students need suitable section assignments and unattempted exams. A single
account repeatedly shared across 500 virtual users does not model 500 distinct students.

Before high load, confirm the approved deployment and test window, current hosting restrictions,
database connection and resource headroom, monitoring, numeric stop thresholds and who will stop
the test. Prefer isolated staging. Review login writes and audit-log volume. See the README's
confirmation checklist. No resource-based automatic stop is implemented; use Ctrl+C when agreed
thresholds or real-user impact require stopping.

## 13. How to increase the number of users

Change `-u` to set the target concurrent student count. Change `-r` to control how many users start
per second. For example, `-u 5 -r 1` targets five concurrent students and starts one per second.
The `-t` duration includes this ramp period.

Before requesting five users, provide at least five existing, unique dedicated STUDENT accounts in
`accounts.csv`, using the `email,password` header described above. Set:

```dotenv
ACCOUNTS_CSV=accounts.csv
ALLOW_SHARED_ACCOUNT=false
REQUIRE_DATA=true
```

From PowerShell in this folder, run:

```powershell
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 5 -r 1 -t 2m --csv results\users5 --csv-full-history --html results\users5.html --exit-code-on-error 1
Start-Process .\results\users5.html
```

Increase gradually: **1 → 5 → 10 → 25** users. For each stage, provide at least that many account
rows, change `-u`, and use a new CSV/HTML prefix such as `results\users10`. Run one stage at a time.
Resolve the missing-subject coverage issue first, then review HTTP failures, route coverage, achieved
user count, response times and Render/database metrics after every stage. A two-minute run is an
initial check, not proof of sustained capacity. Stop with Ctrl+C if agreed thresholds or real-user
impact require it. The hosting, database and test-window prerequisites above apply to increased load.
Before proceeding to 100–500 users, explicitly confirm the high-load prerequisites.

## 14. Approved staged load commands

Run these only after smoke coverage succeeds and the high-load prerequisites are confirmed.
Review each stage and allow recovery before continuing. These commands are not automatically run.

```powershell
# 100 users, 2 new users/sec, 10 minutes total (50-second ramp).
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 100 -r 2 -t 10m --csv results\users100 --csv-full-history --html results\users100.html --exit-code-on-error 1

# 250 users, 2 new users/sec, 15 minutes total (125-second ramp).
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 250 -r 2 -t 15m --csv results\users250 --csv-full-history --html results\users250.html --exit-code-on-error 1

# 500 users, 2 new users/sec, 20 minutes total (250-second ramp).
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 500 -r 2 -t 20m --csv results\users500 --csv-full-history --html results\users500.html --exit-code-on-error 1
```

500 virtual users is a requested workload, not a proven application capacity. A successful test
supports conclusions only for the measured environment, data, workload, duration and acceptance criteria.

## 15. Record each run

Keep the following alongside the reports, without credentials or tokens:

```text
Date/time and timezone:
Backend deployment version:
Render and database plans:
Target origin:
Requested users / achieved users:
Spawn rate / total duration / steady-state period:
Think-time range / REQUIRE_DATA setting:
Routes exercised and skipped:
HTTP failures / COVERAGE failures:
Per-route latency and throughput:
Server/database/generator resource observations:
Stop conditions reached:
Conclusion and next action:
```
