# Test Flow

This document describes the recommended end-to-end test flow for the system.

## 1. Candidate Application

1. Open the frontend.
2. Register a new candidate with a valid test TCKN.
3. Login as the candidate.
4. Create an application for a department, for example Music Teaching.
5. Fill TYT, OBP, performance preferences and required files.
6. Submit the application.

Expected result:

- Candidate sees the submitted application in the applicant dashboard.
- Application status is visible.

## 2. Department Admin Review

1. Login as the related department admin.
2. Open the applications list.
3. Verify that only the admin's departments are visible.
4. Open the new candidate application.

Expected result:

- Music admin sees music applications.
- Sport admin sees sport applications.
- Art admin sees art and ceramic applications.
- Cross-field applications are not visible.

## 3. Candidate-Jury Assignment

1. Open the candidate detail.
2. Generate jury suggestions.
3. Verify that suggested juries belong to the same field and department.
4. Approve exactly 3 juries.

Expected result:

- Music candidates receive only music juries.
- Sport candidates receive only sport juries.
- Art and ceramic candidates receive the correct art/ceramic scope.
- No duplicate assignment error appears.

## 4. Exam Scheduling

1. Open Exam Planning.
2. Create an exam session for the candidate's department.
3. Assign candidates to the session.
4. Publish the session.

Expected result:

- Only candidates with 3 approved jury assignments are placed.
- Candidate receives exam date, time, room and order.
- Published schedule appears in the applicant dashboard.

## 5. Exam Admission Document

1. Login as the candidate.
2. Open the exam admission document.

Expected result:

- PDF opens successfully.
- Candidate identity, department, exam date, time, room and order are visible.
- Document is a single-page official-looking admission document.

## 6. Jury Evaluation

1. Login as each assigned jury.
2. Open the candidate from the jury dashboard.
3. Review candidate details and documents.
4. Enter scores and confirm.

Expected result:

- Jury sees only assigned candidates.
- Jury can score candidates assigned to them.
- A jury cannot score the same candidate twice.
- After 3 jury scores, the candidate average is calculated.

## 7. Result Review

1. Login as department admin or superadmin.
2. Open the candidate.
3. Check jury scores and calculated average.
4. Login as the candidate and open the result document.

Expected result:

- Candidate status becomes completed after 3 scores.
- Result score is visible to admin/superadmin.
- Result document opens for the candidate when results are published.
