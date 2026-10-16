"""Student reads using repository API contracts; no answer/exam submissions."""
import csv
import os
import random
import re
from collections import deque
from pathlib import Path
from urllib.parse import urlsplit

from dotenv import load_dotenv
from locust import HttpUser, between, events, task
from locust.exception import StopUser

ROOT = Path(__file__).resolve().parent
load_dotenv(ROOT / ".env")
TARGET_HOST = os.getenv("TARGET_HOST", "https://projects-v8fc.onrender.com").rstrip("/")
TIMEOUT = float(os.getenv("REQUEST_TIMEOUT", "30"))
REQUIRE_DATA = os.getenv("REQUIRE_DATA", "true").lower() == "true"
SHARED = os.getenv("ALLOW_SHARED_ACCOUNT", "false").lower() == "true"
MIN_WAIT = float(os.getenv("THINK_MIN", "2"))
MAX_WAIT = float(os.getenv("THINK_MAX", "5"))
if TIMEOUT <= 0 or not 0 <= MIN_WAIT <= MAX_WAIT:
    raise ValueError("Invalid timeout or think-time range")


def validate_host(host):
    parsed = urlsplit(host)
    if (parsed.scheme not in ("http", "https") or not parsed.hostname
            or parsed.username or parsed.password or parsed.query or parsed.fragment
            or parsed.path not in ("", "/")):
        raise ValueError("Backend host must be an HTTP(S) origin without credentials/path")
    if parsed.hostname == "hintmatrix-project.onrender.com":
        raise ValueError("Frontend URL cannot be used as the backend host")


def read_accounts():
    filename = os.getenv("ACCOUNTS_CSV")
    if filename:
        path = Path(filename)
        if not path.is_absolute():
            path = ROOT / path
        with path.open(newline="", encoding="utf-8-sig") as stream:
            rows = list(csv.DictReader(stream))
    else:
        rows = [{"email": os.getenv("TEST_EMAIL"), "password": os.getenv("TEST_PASSWORD")}]
    if not rows or any(not r.get("email") or not r.get("password") for r in rows):
        raise ValueError("Provide TEST_EMAIL/TEST_PASSWORD or a valid ACCOUNTS_CSV")
    if len({r["email"] for r in rows}) != len(rows):
        raise ValueError("Account CSV contains duplicate emails")
    return rows


@events.test_start.add_listener
def prepare_accounts(environment, **kwargs):
    if environment.parsed_options.master or environment.parsed_options.worker:
        raise ValueError("Use a single Locust process; account allocation is process-local")
    validate_host(environment.host or TARGET_HOST)
    rows = read_accounts()
    count = environment.parsed_options.num_users
    if count and count > len(rows) and not SHARED:
        raise ValueError("Insufficient dedicated accounts for requested user count")
    environment.student_accounts = deque(rows)


class StudentUser(HttpUser):
    host = TARGET_HOST
    wait_time = between(MIN_WAIT, MAX_WAIT)

    def on_start(self):
        pool = self.environment.student_accounts
        if not pool:
            raise StopUser()
        self.account = pool.popleft()
        if SHARED:
            pool.append(self.account)
        if not self.login():
            raise StopUser()

    def on_stop(self):
        if hasattr(self, "account") and not SHARED:
            self.environment.student_accounts.append(self.account)

    def login(self):
        self.client.headers.pop("Authorization", None)
        with self.client.post("/api/auth/login", json=self.account,
                              name="POST /api/auth/login", timeout=TIMEOUT,
                              allow_redirects=False, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"Login HTTP {response.status_code}")
                return False
            try:
                data = response.json()
                valid = (isinstance(data, dict) and isinstance(data.get("token"), str)
                         and bool(data["token"]) and data.get("tokenType") == "Bearer"
                         and data.get("role") == "STUDENT" and data.get("userId") is not None)
            except ValueError:
                valid = False
            if not valid:
                response.failure("Login contract mismatch: expected STUDENT and Bearer token")
                return False
            self.client.headers["Authorization"] = "Bearer " + data["token"]
            response.success()
            return True

    def get_data(self, path, key, *, many=True, params=None, expected_id=None):
        expired = False
        data = None
        # Constant names aggregate random IDs without exposing tokens or bodies.
        name = re.sub(r"/\d+(?=/|$)", "/{id}", path)
        with self.client.get(path, params=params, name="GET " + name, timeout=TIMEOUT,
                             allow_redirects=False, catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"HTTP {response.status_code}")
                expired = response.status_code == 401
            else:
                try:
                    candidate = response.json()
                    rows = candidate if many else [candidate]
                    valid = (isinstance(rows, list) and (bool(rows) or not REQUIRE_DATA)
                             and all(isinstance(row, dict)
                                     and isinstance(row.get(key), int)
                                     and not isinstance(row[key], bool)
                                     and row[key] > 0 for row in rows))
                    if expected_id is not None:
                        valid = valid and candidate.get(key) == expected_id
                    if params:
                        valid = valid and all(all(row.get(k) == v for k, v in params.items())
                                              for row in rows)
                    if not valid:
                        raise ValueError()
                    data = candidate
                    response.success()
                except (ValueError, TypeError, AttributeError):
                    response.failure("JSON contract mismatch or required data empty")
        if expired and not self.login():
            raise StopUser()
        return data

    def pause(self):
        self.wait()

    @task(3)
    def study(self):
        courses = self.get_data("/api/course", "courseId")
        if not courses:
            return
        course = random.choice(courses)
        self.pause()
        subjects = self.get_data("/api/subjects", "subjectId")
        if subjects is None:
            return
        subjects = [s for s in subjects if s.get("courseId") == course["courseId"]]
        if not subjects:
            self.coverage_failure("course has no related subjects")
            return
        subject = random.choice(subjects)
        self.pause()
        chapters = self.get_data("/api/chapter", "chapterId")
        if chapters is None:
            return
        chapters = [c for c in chapters if c.get("courseId") == course["courseId"]
                    and c.get("subjectId") == subject["subjectId"]]
        if not chapters:
            self.coverage_failure("subject has no related chapters")
            return
        chapter = random.choice(chapters)
        self.pause()
        topics = self.get_data("/api/topics", "topicId")
        if topics is None:
            return
        topics = [t for t in topics if t.get("courseId") == course["courseId"]
                  and t.get("chapterId") == chapter["chapterId"]]
        if not topics:
            self.coverage_failure("chapter has no related topics")
            return
        topic = random.choice(topics)
        self.pause()
        questions = self.get_data("/api/questions/filter", "questionId", params={
            "courseId": course["courseId"], "chapterId": chapter["chapterId"],
            "topicId": topic["topicId"]})
        if questions:
            self.pause()
            qid = random.choice(questions)["questionId"]
            self.get_data(f"/api/questions/{qid}", "questionId", many=False, expected_id=qid)

    def coverage_failure(self, reason):
        if REQUIRE_DATA:
            self.environment.events.request.fire(request_type="COVERAGE", name=reason,
                                                 response_time=0, response_length=0,
                                                 exception=ValueError(reason))

    @task(1)
    def exams(self):
        exams = self.get_data("/api/exams", "examId")
        if not exams:
            return
        eid = random.choice(exams)["examId"]
        self.pause()
        detail = self.get_data(f"/api/exams/{eid}", "examId", many=False, expected_id=eid)
        if detail is not None:
            self.pause()
            self.get_data(f"/api/exams/{eid}/questions", "questionId")
