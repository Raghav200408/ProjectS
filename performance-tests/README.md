# Hintmatrix backend load tests

For a file-by-file implementation walkthrough, beginner PowerShell steps, report explanations and
troubleshooting, see [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md).

These files exercise the existing Spring Boot API. Default backend: `https://projects-v8fc.onrender.com`.
Frontend: `https://hintmatrix-project.onrender.com` (informational only; never an API target).
No frontend, business logic, schema, or deployment changes are required. No capacity claim is made.

## Verified repository contracts

Sources: `src/main/java/com/project/ProjectS/controller/{Auth,Course,Subject,Chapter,Topic,Question,Exam}Controller.java`,
the corresponding response DTOs and services, `config/SecurityConfig.java`,
`security/filter/JwtAuthenticationFilter.java`, and `security/jwt/JwtUtil.java`.

| Request | Successful response | Access |
| --- | --- | --- |
| POST `/api/auth/login` | 200 object: token, tokenType=Bearer, userId, name, email, role | Public; JSON `{ "email": "...", "password": "..." }` |
| GET `/api/course` | 200 array; courseId, name | Authenticated supported roles |
| GET `/api/subjects` | 200 array; subjectId, subjectName, courseId | Same |
| GET `/api/chapter` | 200 array; chapterId, name, courseId, subjectId | Same |
| GET `/api/topics` | 200 array; topicId, name, courseId, subjectId, chapterId | Same |
| GET `/api/questions/filter` | 200 question array | Same; required query: courseId, chapterId, topicId |
| GET `/api/questions/{questionId}` | 200 question object; questionId, questionText, mapping IDs | Same |
| GET `/api/exams` | 200 exam array; examId, examName, courseId, sectionId | Student/admin; guests denied |
| GET `/api/exams/{examId}` | 200 exam object | Student/admin |
| GET `/api/exams/{examId}/questions` | 200 question array | Student/admin |

Catalogue routes also have GET `/{id}`. GET `/api/questions` and case-sensitive
`/api/questions/QuestionText` exist but are intentionally not used for repeated full-bank downloads.
There are no catalogue parent-filter query parameters in these controllers: catalogue lists are
fetched in full and related records selected locally. Question filtering returns active questions.
Student exam lists use the student's section and exclude attempted exams. A student with no section
or unattempted assigned exams may receive an empty array. Course entitlement checks in the inspected
question-filter and exam-question paths are commented out; deployment behavior must be verified by smoke.

Security is stateless JWT, not session-cookie login. Every protected request sends the returned Bearer token.
JWT lifetime comes from backend configuration. A 401 is recorded as failure and triggers one login;
the failed request is not hidden/retried. No retries for 403/429/5xx, redirects, or timeouts.
Login updates `lastLoginAt` and writes audit logs. No account creation, answer submission,
exam submission, uploads, activation, or deletion is performed.

## Workload and validation

Each virtual user logs in at startup. Task selection is 3:1 study/exam journeys (not a request ratio).
Study: courses → related subject → related chapter → related topic → filtered questions → random question detail.
Exam: assigned exams → random exam detail → exam questions. Think time defaults to 2–5 seconds between
reads and between journeys. This models browsing students, not 500 simultaneous requests or a login storm
beyond the configured ramp. No frontend assets are fetched.

Every HTTP request requires 200 and valid JSON. Lists require positive integer IDs on every record;
detail IDs must match the requested ID, filtered question mappings must match all query IDs, and login
must return a nonempty Bearer token for a STUDENT. Empty lists fail by default. Missing hierarchy links
produce separate `COVERAGE` failures with zero duration: exclude those synthetic events when evaluating
HTTP latency/throughput. Set `REQUIRE_DATA=false` only for intentional empty content; skipped paths then
reduce coverage. Verify every route appears in smoke statistics before considering a load run valid.
Random selection can encounter incomplete courses: prepare representative complete test content first.

Locust reports response times/percentiles, requests/sec and failures per stable endpoint name;
CSV and HTML persist results. Failure messages omit bodies, passwords and tokens. Protect results anyway.
This setup supports one Locust process only, not master/worker or multiple processes. Account allocation
is local; provide one unique dedicated student per concurrent user. `ALLOW_SHARED_ACCOUNT=true` is an
explicit smoke/debug override; sharing users distorts database contention and student scope.

## Windows PowerShell setup

Run from the backend repository root. Install Python 3.11+ if `py` is unavailable.

```powershell
Set-Location .\performance-tests
py -3 -m venv .venv
# Direct executable calls avoid PowerShell activation-policy changes.
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
Copy-Item .env.example .env
New-Item -ItemType Directory -Force results | Out-Null
```

Edit `.env` locally with dedicated test credentials. For smoke, set TEST_EMAIL and TEST_PASSWORD.
For load, set `ACCOUNTS_CSV=accounts.csv`, whose header is `email,password` and whose subsequent rows
contain pre-provisioned dedicated students (CSV quote values containing commas). Never commit this file.
Provisioning is outside this setup; do not create accounts through a fabricated endpoint.
Environment variables override `.env`. Relative CSV paths resolve against this directory.

## Cold start first, then low-load smoke

Measure a single unauthenticated request to the real profile endpoint. Its expected status is 401,
so it tests backend wake-up without credentials or invented health endpoints. It does not prove DB readiness.
The first request may already be warm. Record its date/time and Render idle state.

```powershell
$backend = 'https://projects-v8fc.onrender.com'
curl.exe --silent --show-error --max-time 180 --output NUL --write-out 'status=%{http_code} total_seconds=%{time_total}\n' "$backend/api/auth/me" | Tee-Object results\wake-up.txt
```

If this times out or returns an unexpected status, inspect the service before proceeding. Once awake,
run the one-user smoke (login also verifies database/authentication readiness):

```powershell
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 1 -r 1 -t 2m --csv results\smoke --csv-full-history --html results\smoke.html --exit-code-on-error 1
```

Do not proceed until login and all intended routes succeed with representative data. Random task
selection does not guarantee exam coverage within two minutes: inspect stats and extend smoke if needed.
This smoke has NOT been executed by the implementation agent: credentials are required.

## Required confirmation before high load

Confirm with the service/database owner that this deployment may receive synthetic traffic, and choose
an approved quiet window. Prefer an isolated staging deployment and database. Confirm current Render
plan limits, bandwidth/usage costs, autoscaling behavior and any provider restrictions from the account's
current plan/terms; no plan capacity is assumed here. Confirm database connection limits, pool headroom,
CPU/memory/storage/IO capacity, backups and monitoring access. JWT authentication reloads users from the
database on protected requests, and the inspected question service loads related attributes/pairs/blanks;
these paths may stress DB queries before CPU saturates.

Arrange monitoring for Render CPU, memory, restarts, logs, database connections/query latency and real-user
errors. Agree on numeric latency/error/resource stop thresholds and a person able to stop the test.
A possible starting policy to review: stop for sustained HTTP errors >1%, p95 >2 seconds, resource use
>80%, connection exhaustion, or any real-user impact. These are proposed thresholds, not application SLOs.
Use Ctrl+C to stop; thresholds/resource stops are not automated by these files. Confirm 100/250/500 unique
student accounts, section assignments and representative unattempted exams. Review audit-log volume and
last-login writes. Never use real students' passwords. Reconfirm permission if the target changes.

Only after approval and successful smoke, run one stage at a time, review results and allow recovery
between stages. The commands below are instructions, not automatically executed tests:

```powershell
# 100 users: 50-second ramp; total duration includes ramp.
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 100 -r 2 -t 10m --csv results\users100 --csv-full-history --html results\users100.html --exit-code-on-error 1
# 250 users: 125-second ramp.
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 250 -r 2 -t 15m --csv results\users250 --csv-full-history --html results\users250.html --exit-code-on-error 1
# 500 users: 250-second ramp.
.\.venv\Scripts\python.exe -m locust -f locustfile.py --headless -u 500 -r 2 -t 20m --csv results\users500 --csv-full-history --html results\users500.html --exit-code-on-error 1
```

Adjust `-u`, `-r`, `-t`, CSV/HTML prefixes and think times for an approved workload. Use unique prefixes
to avoid overwriting prior runs. TARGET_HOST changes the backend default; `--host` overrides it and is
validated against the known frontend hostname. Other frontend aliases remain the operator's responsibility.
For the optional web UI, omit `--headless`, open localhost:8089 and start with one user; ensure enough
accounts before increasing users. Headless preflight checks the requested account count.

## Interpret results

Keep wake-up and smoke results separate from load runs. Load CSV history still contains login and ramp:
analyze history only after full user count has stabilized, using recorded timestamps, and report login
separately from protected reads. Full-run aggregate percentiles include ramp; they are not steady-state
percentiles. Use a time-window-aware analysis of history for steady-state rate/error trends; history
percentile fields are Locust's rolling/cumulative statistics, not an exact recomputed window percentile.
Retain raw observations or add approved telemetry if exact steady-state percentiles are needed.

Record deployment version, Render/DB plan, test time, user count, spawn rate, duration, achieved users,
request rates, per-route p50/p95/p99, HTTP and coverage failures, generator utilization and server/DB metrics.
Check the generator is not the bottleneck. A failed login stops that user; verify achieved user count
instead of assuming the requested count was sustained. Any failed request gives a nonzero exit code.
Passing 500 virtual users under this browsing mix only establishes observed performance for that test's
data, environment and duration; it does not establish general production capacity. No live capacity
results exist from this implementation.
