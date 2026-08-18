package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.edu.inonu.oys.model.ExamSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByDepartmentIdOrderByExamDateAscStartTimeAsc(Long departmentId);
    List<ExamSession> findAllByOrderByExamDateAscStartTimeAsc();

    @Query("""
            SELECT s FROM ExamSession s
            WHERE s.classroom.id = :classroomId
              AND s.examDate = :examDate
              AND s.startTime < :endTime
              AND s.endTime > :startTime
            """)
    List<ExamSession> findClassroomConflicts(@Param("classroomId") Long classroomId,
                                             @Param("examDate") LocalDate examDate,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime);
}
