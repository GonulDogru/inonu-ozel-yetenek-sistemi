package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.CandidateJuryAssignment;

import java.time.LocalDateTime;

public class CandidateJuryAssignmentDTO {
    private final Long id;
    private final Long applicationId;
    private final String applicantFullName;
    private final Long juryId;
    private final String juryFullName;
    private final String status;
    private final int matchScore;
    private final String matchedAreas;
    private final LocalDateTime approvedAt;

    public CandidateJuryAssignmentDTO(CandidateJuryAssignment assignment) {
        this.id = assignment.getId();
        this.applicationId = assignment.getApplication().getId();
        this.applicantFullName = assignment.getApplication().getApplicant() != null
                ? assignment.getApplication().getApplicant().getFirstName() + " " + assignment.getApplication().getApplicant().getLastName()
                : "-";
        this.juryId = assignment.getJury().getId();
        this.juryFullName = assignment.getJury().getFirstName() + " " + assignment.getJury().getLastName();
        this.status = assignment.getStatus().name();
        this.matchScore = assignment.getMatchScore();
        this.matchedAreas = assignment.getMatchedAreas();
        this.approvedAt = assignment.getApprovedAt();
    }

    public Long getId() { return id; }
    public Long getApplicationId() { return applicationId; }
    public String getApplicantFullName() { return applicantFullName; }
    public Long getJuryId() { return juryId; }
    public String getJuryFullName() { return juryFullName; }
    public String getStatus() { return status; }
    public int getMatchScore() { return matchScore; }
    public String getMatchedAreas() { return matchedAreas; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
}
