# Manual tests: course assignment and subscriptions

Test date: __________  Tester: __________  Backend revision: __________  Frontend revision: __________

These are manual acceptance cases, not a record of tests already executed. Mark each case **Pass**, **Fail**, or **Blocked** and record evidence. A hidden button alone does not prove backend access control.

## 1. Preparation

1. Use a local or staging database with test accounts. Deactivation and expiry cases must use disposable records.
2. Apply `src/main/java/DataBase/courseassignment.sql`. Start the updated backend and frontend. These instructions do not execute migrations.
3. Use separate browser profiles or private sessions for administrators and students. Refresh after changing assignments or subscriptions.
4. Create the fixture below. Record real IDs; do not assume generated IDs.
5. For rejected saves, verify that data did not change. If the interface displays a generic error, use the browser Network response and backend log to identify the reason.

### Test fixture

| Label | Record to prepare | Actual ID / login |
| --- | --- | --- |
| SA | Super Admin | |
| CA-A | College Admin for College A | |
| BA-A1 | Branch Admin for College A / A1 | |
| BA-B1 | Branch Admin for College B / B1 | |
| A1 | First branch in College A | |
| A2 | Second branch in College A; initially unassigned | |
| B1 | Branch in College B | |
| C1 | New course named QA Shared Commerce | |
| C2 | Separate course named QA Other Course | |
| Q1–Q4 | Four active questions in C1, with shared subject/chapter/topic content | |
| QX | Active question in C2 | |
| S-A | Student in A1, enrolled in C1 / Section A | |
| S-A2 | Another student in A1, enrolled in C1 / Section A | |
| S-B | Student in B1, enrolled in C1 / Section A | |

Create students after assigning C1 to A1 and B1 and creating their sections. Both branches may use the name Section A, but must have separate section IDs.

### Plans to prepare

Use paid test activation where appropriate; these tests do not require a payment-gateway transaction.
Map each plan to C1 and use a positive duration, such as 30 days. These plans grant course-level access. There is no per-plan question-selection screen or tier inclusion rule on this test branch.

| Plan name | Practice usage limit | Mock tests | Exam attempts |
| --- | --- | --- | --- |
| QA Limited | 2 | Enabled; limit 1 | Enabled; limit 1 |
| QA Unlimited | -1 | Enabled; limit -1 | Enabled; limit -1 |
| QA Practice Only | 2 | Disabled | Disabled |

Use fresh personal subscriptions when testing counters. C1 should contain Q1–Q4 so you can test the practice limit without exhausting the course's content first.

## 2. Course creation and branch assignments

### CA-01 — Create a shared course before selecting an institution
- **Role / setup:** SA; C1 does not exist yet.
- **Steps:** Open Courses → Add Course. Enter QA Shared Commerce and save without a college or branch. Add its subject, chapter, topic and Q1–Q4 once.
- **Expected:** Creation succeeds. C1 has one course ID and can be assigned later. Content belongs to that same course ID.
- **Result:** __________ **Actual / evidence:** __________

### CA-02 — Assign to the first branch
- **Role / setup:** SA; active C1 and A1.
- **Steps:** Open C1 → Assign to branch. Select College A / A1 and click Assign branch. Close, refresh and reopen the assignments.
- **Expected:** A1 appears as Assigned. The assignment persists after refresh.
- **Result:** __________ **Actual / evidence:** __________

### CA-03 — Reuse the same course across colleges
- **Role / setup:** SA; CA-02 passed.
- **Steps:** Assign C1 to College B / B1. Open the course from A1 and B1 administrator sessions; compare course, subject, chapter and question IDs in Network responses.
- **Expected:** Both branches use the same C1 and content IDs. Assignment does not create duplicate courses or content.
- **Result:** __________ **Actual / evidence:** __________

### CA-04 — Assignment is per branch
- **Role / setup:** A1 assigned; A2 unassigned.
- **Steps:** Select College A / A2 in student, section and exam forms. Check C1 availability. Compare with A1.
- **Expected:** C1 is available for A1 and unavailable for A2. Assigning A1 does not automatically assign every branch in College A. An admin course catalog may still show unassigned courses for assignment purposes.
- **Result:** __________ **Actual / evidence:** __________

### CA-05 — Repeated assignment does not create duplicates
- **Role / setup:** SA; A1 already assigned.
- **Steps:** Assign C1 to A1 again; refresh. Check the assignment list and read-only database query below.
- **Expected:** One C1/A1 mapping, one shared course, and no duplicate content. An existing mapping is reused.
- **Result:** __________ **Actual / evidence:** __________

### CA-06 — Unassign one branch
- **Role / setup:** C1 assigned to A1 and B1; S-A and S-B have working subscriptions.
- **Steps:** Unassign B1. Refresh both student sessions and try to open/answer a C1 question, including an already-open page.
- **Expected:** S-B loses future learning access even if its subscription remains active. S-A retains access. Existing content, subscriptions and historical results are retained.
- **Result:** __________ **Actual / evidence:** __________

### CA-07 — Reassign without copying content or losing history
- **Role / setup:** Restore after CA-06; S-B's subscription has not expired.
- **Steps:** Click Reassign for B1. Refresh S-B and reopen C1 and its previous results.
- **Expected:** Access returns using the same course/question IDs and assignment row. Previous personal results remain; usage is not reset by reassigning a branch.
- **Result:** __________ **Actual / evidence:** __________

### CA-08 — Local administrators stay within their organization
- **Role / setup:** CA-A and BA-A1 sessions.
- **Steps:** Check assignment choices. CA-A should manage College A branches; BA-A1 should manage A1 only. In a test API client using each account's session, attempt an assignment to B1; BA-A1 also attempts A2.
- **Expected:** Out-of-scope assignments are rejected by the backend and no mapping changes. SA can assign across colleges.
- **Result:** __________ **Actual / evidence:** __________

### CA-09 — Duplicate course creation and central content editing
- **Role / setup:** C1 exists; SA, CA-A and BA-A1 sessions.
- **Steps:** As SA, try creating another course with the same name, including different capitalization. As local admins, check shared course/content create/edit actions.
- **Expected:** Duplicate creation is rejected with an instruction to assign the existing course. Local admins cannot edit shared content; SA can edit it once for all assigned branches.
- **Result:** __________ **Actual / evidence:** __________

## 3. Student, section and exam forms

### EF-01 — Course appears in the actual exam creation page
- **Role / setup:** SA; C1 assigned to B1 although its original/legacy branch is A1, or it has no legacy branch.
- **Steps:** Open the exam creation page with Exam Name, College, Branch, Course, Subject and Duration fields. Select College B → B1 → open Course.
- **Expected:** QA Shared Commerce appears and is selectable using its original shared C1 ID. The dropdown uses assignments, not the course's legacy college/branch fields.
- **Result:** __________ **Actual / evidence:** __________

### EF-02 — Switching branches clears stale selections
- **Role / setup:** SA; select B1, C1, a section, subject and chapter.
- **Steps:** Change the college/branch to unassigned A2. Then change to assigned A1.
- **Expected:** Previous course/section/subject/chapter selections clear. A2 cannot select C1. A1 can select C1 with its own sections.
- **Result:** __________ **Actual / evidence:** __________

### EF-03 — Same section name in two assigned branches
- **Role / setup:** C1 assigned to A1 and B1.
- **Steps:** Create Section A under A1/C1 and under B1/C1. Try creating Section A again under A1/C1. In the exam form, switch between A1 and B1.
- **Expected:** The first two sections save with different section IDs. The duplicate within A1/C1 is rejected. Each branch's exam dropdown contains only its own sections.
- **Result:** __________ **Actual / evidence:** __________

### EF-04 — Enrollment follows assignments and section ownership
- **Role / setup:** A1/B1 assigned; A2 unassigned.
- **Steps:** Create S-A and S-B in their correct C1 sections. Try enrolling a test student in A2/C1 or submitting an A1 student with B1's section ID using a test API client.
- **Expected:** Valid enrollment succeeds. Unassigned courses and inconsistent college/branch/course/section combinations are rejected.
- **Result:** __________ **Actual / evidence:** __________

### EF-05 — Create a branch exam with shared content
- **Role / setup:** SA; B1 has C1 and its own Section A.
- **Steps:** Create a B1 exam using C1, its subject/chapter and B1's Section A. Select questions and save. Reopen/edit the exam.
- **Expected:** Save succeeds, selections persist, and questions refer to shared C1 content. Editing uses the same assignment-aware options.
- **Result:** __________ **Actual / evidence:** __________

### EF-06 — Exam audience stays branch/section specific
- **Role / setup:** EF-05 passed; S-A and S-B have an exam-enabled subscription.
- **Steps:** Open/attempt the B1 exam as S-B. As S-A, try its saved URL and question/submit requests in a test API client.
- **Expected:** S-B can participate during the valid exam window. S-A cannot access or submit B1's exam despite sharing C1. No unauthorized result or quota charge is created.
- **Result:** __________ **Actual / evidence:** __________

### EF-07 — Mock exam creation remains independent of branch selection
- **Role / setup:** SA; active C1.
- **Steps:** Open the mock-exam creation page. Select C1 without selecting a college or branch and configure questions.
- **Expected:** C1 remains available. The branch-only exam dropdown fix does not impose institution fields on mock-exam creation.
- **Result:** __________ **Actual / evidence:** __________

## 4. Course plans and activation

### SU-01 — Branch assignment alone does not grant student question access
- **Role / setup:** C1 assigned to A1; a fresh A1 student has no C1 subscription.
- **Steps:** Try opening and answering a C1 question through the UI and saved question URL.
- **Expected:** Learning access is denied until a valid subscription is activated. Administrator preview access is not used for this test.
- **Result:** __________ **Actual / evidence:** __________

### SU-02 — Bulk assignment creates individual student subscriptions
- **Role / setup:** BA-A1; S-A and S-A2 eligible for C1.
- **Steps:** Open Subscriptions → Assign a course plan to students. Select C1, QA Limited and S-A/S-A2. Assign and check history and each student's own subscription response.
- **Expected:** Both receive separate subscription IDs with their own dates and counters. Assigning a plan does not create a single subscription shared by the branch.
- **Result:** __________ **Actual / evidence:** __________

### SU-03 — Same course/plan in another college stays personal
- **Role / setup:** BA-B1; S-B eligible for C1.
- **Steps:** Assign QA Limited/C1 to S-B. Compare with S-A's subscription and course IDs.
- **Expected:** Both reference the same C1 and plan, but subscription IDs, dates, counters and results belong to each student individually.
- **Result:** __________ **Actual / evidence:** __________

### SU-04 — Reject subscription activation for an unassigned branch or wrong course plan
- **Role / setup:** A2 unassigned; test API client with authorized test admin session.
- **Steps:** Attempt to activate C1 for a test student in A2. Separately try activating a C2-only plan for C1.
- **Expected:** Both requests fail. No new subscription is created and an existing valid subscription is not replaced.
- **Result:** __________ **Actual / evidence:** __________

### SU-05 — Replacing a student's subscription preserves others and history
- **Role / setup:** S-A and S-A2 have QA Limited/C1.
- **Steps:** Assign QA Unlimited/C1 to S-A only. Inspect S-A's current subscription and history, then S-A2's.
- **Expected:** S-A gets a new subscription; the prior active C1 subscription is deactivated and retained in history. S-A2 is unchanged. New-subscription counters are separate from the previous subscription's counters.
- **Result:** __________ **Actual / evidence:** __________

### SU-06 — Local administrators cannot subscribe another institution's student
- **Role / setup:** BA-A1; S-B belongs to B1.
- **Steps:** Using BA-A1's session, attempt a bulk activation request containing S-B. Also test an invalid batch containing S-A and S-B; inspect subscriptions afterward.
- **Expected:** The out-of-scope request fails. The invalid batch does not partially activate valid members. No unauthorized student subscription changes.
- **Result:** __________ **Actual / evidence:** __________

## 5. Usage limits, expiry and isolation

### US-01 — First use of distinct practice questions consumes quota
- **Role / setup:** Fresh QA Limited subscription for S-A; practice usage starts at 0.
- **Steps:** Answer Q1, then Q2, checking the student's counters after each. Attempt Q3.
- **Expected:** Usage becomes 1 then 2. First use of Q3 is rejected because the usage limit is 2 even though Q3 belongs to C1. Rejection does not increase the counter.
- **Result:** __________ **Actual / evidence:** __________

### US-02 — Retries and multiple adjustment effects do not double-charge
- **Role / setup:** Fresh QA Limited subscription; Q1 is preferably a question with multiple attributes/effects.
- **Steps:** Make an incorrect attempt on Q1, retry, request a hint/autofill if available, and place its remaining effects. Refresh and continue Q1.
- **Expected:** Q1 consumes one distinct-question usage entry for that subscription. Repeated effects, hints, retries, and separate event/answer requests for Q1 do not charge additional questions. Scoring still follows the question type's normal rules.
- **Result:** __________ **Actual / evidence:** __________

### US-03 — Usage and results remain independent
- **Role / setup:** S-A has exhausted its QA Limited quota; S-A2 and S-B have fresh subscriptions.
- **Steps:** Answer Q1 as S-A2 and S-B. Compare usage, results, progress and dashboard values across all three accounts.
- **Expected:** Each student's usage increases independently. S-A's exhausted quota and answers do not consume the others' quota or prefill their personal answers/results.
- **Result:** __________ **Actual / evidence:** __________

### US-04 — Unlimited practice usage
- **Role / setup:** Fresh QA Unlimited subscription for a disposable student.
- **Steps:** Answer Q1, Q2, Q3 and Q4.
- **Expected:** All four are permitted; a -1 limit does not behave as zero or two. Personal usage still records activity.
- **Result:** __________ **Actual / evidence:** __________

### US-05 — Disabled modules are denied
- **Role / setup:** QA Practice Only subscription; C1 assigned.
- **Steps:** Answer an included practice question. Try starting a mock test and attempting/submitting an otherwise valid C1 exam using UI and relevant saved requests.
- **Expected:** Practice works within quota. Disabled mock/exam activity is rejected; no successful attempt/result is recorded for the denied operation.
- **Result:** __________ **Actual / evidence:** __________

### US-06 — Mock-test and exam-attempt limits
- **Role / setup:** Fresh QA Limited subscription; valid same-section exam and accessible questions.
- **Steps:** Start one mock test, then attempt to start another. Complete/submit one exam attempt and attempt another valid exam submission. Check counters after each operation.
- **Expected:** First allowed operations consume one of their respective counters. Further operations exceeding each limit are rejected. Failed requests do not consume additional quota. Observe the workflow's consumption point: mock-test start and exam submission.
- **Result:** __________ **Actual / evidence:** __________

### US-07 — Expired and future subscriptions cannot grant access
- **Role / setup:** A separate expired subscription fixture and a future-start fixture prepared in the test database by an authorized tester; another student remains valid. The current UI need not expose date editing.
- **Steps:** Open questions and attempt practice/mock/exam operations for each fixture, including already-open pages. Compare with the valid student.
- **Expected:** Expired and not-yet-started subscriptions are denied. Valid students remain unaffected. If fixtures cannot be prepared, mark this case Blocked rather than Pass.
- **Result:** __________ **Actual / evidence:** __________

### US-08 — Deactivate only one student's subscription
- **Role / setup:** BA-A1; S-A and S-A2 have active subscriptions. S-A is a disposable test account.
- **Steps:** In subscription history, deactivate S-A's subscription. Refresh S-A and S-A2 and try C1 practice.
- **Expected:** S-A loses access through that subscription; S-A2 retains access. History and the branch's C1 assignment remain.
- **Result:** __________ **Actual / evidence:** __________

### US-09 — Course access is enforced without question selection
- **Role / setup:** Student has a valid C1 subscription and no C2 subscription. C1 is assigned to the student's branch.
- **Steps:** Open and answer a C1 question without configuring any question package. Try QX from C2 using its direct URL and answer request. Generate a C1 mock test.
- **Expected:** C1 works within normal subscription limits. C2 access is denied without a valid eligible C2 subscription. The mock test draws from C1's active questions; no curated question tables or plan-question endpoints are required.
- **Result:** __________ **Actual / evidence:** __________

### US-10 — Persistence after logout and restart
- **Role / setup:** Record assignments, course/content IDs, student subscription IDs, dates, counters and results.
- **Steps:** Log out/in. Restart the local backend and frontend, then reopen the assignment list, exam form, subscription pages and student results.
- **Expected:** Records and access decisions persist. Refresh/restart does not reset quota, duplicate assignments, or restore revoked access.
- **Result:** __________ **Actual / evidence:** __________

### US-11 — Deactivating the shared course or plan has the expected scope
- **Role / setup:** SA; disposable C1/plan fixtures; run last and preserve baseline IDs/results.
- **Steps:** Deactivate the shared course and check learners in both branches; reactivate for the next step. Deactivate a test plan with students in both branches.
- **Expected:** Course deactivation blocks learning access wherever C1 is assigned and retains content/history. Plan deactivation makes that plan unavailable and deactivates its active subscriptions. Reactivating a plan must not be assumed to reactivate old student subscriptions; explicitly activate fresh test subscriptions if needed.
- **Result:** __________ **Actual / evidence:** __________

## 6. Read-only database evidence (optional)

Use these queries in the test database. They do not change data. Filter by your recorded test IDs if the database contains other records.

```sql
-- Confirm each branch refers to the same course ID.
SELECT ca.courseassignment_id, ca.course_id, c.name AS course_name,
       b.college_id, ca.branch_id, b.branch_name, ca.active_row
FROM public.courseassignment ca
JOIN public.courses c ON c.course_id = ca.course_id
JOIN public.branch b ON b.branch_id = ca.branch_id
WHERE c.name = 'QA Shared Commerce'
ORDER BY b.college_id, ca.branch_id;

-- No rows should be returned: duplicate assignment pairs.
SELECT course_id, branch_id, COUNT(*) AS mapping_count
FROM public.courseassignment
GROUP BY course_id, branch_id
HAVING COUNT(*) > 1;

-- Compare individual subscription dates and counters.
SELECT s.subscription_id, s.user_id, s.course_id, s.plan_id,
       s.active, s.starts_at, s.expires_at,
       s.practice_questions_used, s.mock_tests_used, s.exam_attempts_used
FROM public.user_subscriptions s
JOIN public.courses c ON c.course_id = s.course_id
WHERE c.name = 'QA Shared Commerce'
ORDER BY s.user_id, s.subscription_id;

-- Each pair represents one distinct question charged to that subscription.
SELECT u.subscription_id, u.question_id, COUNT(*) AS usage_rows
FROM public.subscription_question_usage u
JOIN public.user_subscriptions s ON s.subscription_id = u.subscription_id
JOIN public.courses c ON c.course_id = s.course_id
WHERE c.name = 'QA Shared Commerce'
GROUP BY u.subscription_id, u.question_id
ORDER BY u.subscription_id, u.question_id;
```

## 7. Current UI observations to record

- Bulk student subscription assignment is currently exposed on the Subscriptions page for Branch Admin. Do not mark a missing College Admin bulk panel as a failure of branch assignment.
- Subscription-history scope/deactivation for College Admin should be recorded as a separate observation: existing logic is branch-based, and a College Admin without a branch may see no history. Do not use that account as the baseline for BA or SA history cases.
- Plans grant course-level access. The subscription-question-access feature, including question selection and tier package rules, has been removed from this test branch.
- Existing duplicated legacy courses are not merged by this migration. Compare IDs for the newly shared test course.

## 8. Execution summary

Total cases: **33** (CA 9 + EF 7 + SU 6 + US 11).

| Run result | Count |
| --- | --- |
| Pass | |
| Fail | |
| Blocked | |
| Not run | |

Recommended first run: CA-01–CA-07 → EF-01–EF-05 → SU-01–SU-03 → US-01–US-03. Then run the remaining negative, expiry and role cases.

### Defect record template

- Case ID / title:
- Role and test account:
- Course / college / branch / subscription IDs:
- Steps and selected values:
- Expected result:
- Actual result:
- Screenshot / Network status and response:
- Backend error and request ID, if available:
- Reproducible after refresh? Yes / No:

After testing, restore deliberately revoked assignments and disabled test records as needed. Do not delete historical results merely to make the tests pass.
