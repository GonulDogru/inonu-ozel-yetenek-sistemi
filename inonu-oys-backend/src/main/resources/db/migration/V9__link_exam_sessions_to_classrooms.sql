ALTER TABLE exam_sessions ADD COLUMN IF NOT EXISTS classroom_id BIGINT;

ALTER TABLE exam_sessions
    ADD CONSTRAINT fk_exam_session_classroom
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id);

CREATE INDEX IF NOT EXISTS idx_exam_sessions_classroom_date ON exam_sessions(classroom_id, exam_date);
