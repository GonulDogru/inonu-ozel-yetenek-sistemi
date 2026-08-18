package tr.edu.inonu.oys.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "exam_sessions")
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamSessionType sessionType;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private LocalTime startTime;

    private LocalTime endTime;
    private String location;
    private String room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    private Integer candidateIntervalMinutes;
    private boolean published;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public ExamSessionType getSessionType() { return sessionType; }
    public void setSessionType(ExamSessionType sessionType) { this.sessionType = sessionType; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public Integer getCandidateIntervalMinutes() { return candidateIntervalMinutes; }
    public void setCandidateIntervalMinutes(Integer candidateIntervalMinutes) { this.candidateIntervalMinutes = candidateIntervalMinutes; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
