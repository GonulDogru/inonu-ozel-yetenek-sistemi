package tr.edu.inonu.oys.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_session_juries")
public class ExamSessionJury {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id", nullable = false)
    private User jury;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JuryAssignmentRole assignmentRole;

    private boolean replacement;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamSession getExamSession() { return examSession; }
    public void setExamSession(ExamSession examSession) { this.examSession = examSession; }
    public User getJury() { return jury; }
    public void setJury(User jury) { this.jury = jury; }
    public JuryAssignmentRole getAssignmentRole() { return assignmentRole; }
    public void setAssignmentRole(JuryAssignmentRole assignmentRole) { this.assignmentRole = assignmentRole; }
    public boolean isReplacement() { return replacement; }
    public void setReplacement(boolean replacement) { this.replacement = replacement; }
}
