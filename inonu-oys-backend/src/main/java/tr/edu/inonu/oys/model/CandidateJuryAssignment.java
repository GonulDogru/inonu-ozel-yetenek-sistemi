package tr.edu.inonu.oys.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_jury_assignments")
public class CandidateJuryAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id", nullable = false)
    private User jury;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateJuryAssignmentStatus status = CandidateJuryAssignmentStatus.SUGGESTED;

    private int matchScore;

    @Column(columnDefinition = "TEXT")
    private String matchedAreas;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    public User getJury() { return jury; }
    public void setJury(User jury) { this.jury = jury; }
    public CandidateJuryAssignmentStatus getStatus() { return status; }
    public void setStatus(CandidateJuryAssignmentStatus status) { this.status = status; }
    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    public String getMatchedAreas() { return matchedAreas; }
    public void setMatchedAreas(String matchedAreas) { this.matchedAreas = matchedAreas; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
