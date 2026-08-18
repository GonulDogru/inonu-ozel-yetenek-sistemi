package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.ExamSessionJury;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ExamSessionJuryRepository extends JpaRepository<ExamSessionJury, Long> {
    List<ExamSessionJury> findByExamSessionId(Long examSessionId);

    List<ExamSessionJury> findByJuryIdOrderByExamSessionExamDateAscExamSessionStartTimeAsc(Long juryId);

    @Query("""
            SELECT js FROM ExamSessionJury js
            JOIN js.examSession s
            WHERE js.jury.id = :juryId
              AND s.examDate = :examDate
              AND s.startTime < :endTime
              AND s.endTime > :startTime
            """)
    List<ExamSessionJury> findJuryConflicts(@Param("juryId") Long juryId,
                                            @Param("examDate") LocalDate examDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);
}
