package tr.edu.inonu.oys.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import tr.edu.inonu.oys.model.Application;

public class ApplicationDTO {
    private final Long id;
    private final String applicantFullName;
    private final String applicantUsername;
    private final Double tytScore;
    private final Double obp;
    private final String faculty;
    private final Long departmentId;
    private final String programName;
    private final String performancePreferences;
    private final String status;
    private final Double oyspScore;
    private final Double standardizedOyspScore;
    private final Double finalPlacementScore;
    private final Integer placementRank;
    private final String placementStatus;
    private final boolean resultPublished;
    private final String osymDocPath;
    private final String diplomaDocPath;
    private final String healthDocPath;
    private final String photoDocPath;
    private final String nationalDocPath;
    private final String disabledDocPath;
    private final Boolean isNational;
    private final Boolean isDisabled;
    private final String examSessionType;
    private final LocalDate examDate;
    private final LocalTime examStartTime;
    private final LocalTime examEndTime;
    private final String examLocation;
    private final String examRoom;
    private final Integer examOrder;
    private final LocalTime appointmentStartTime;
    private final LocalTime appointmentEndTime;
    private final boolean examSchedulePublished;
    private List<CandidateJuryAssignmentDTO> juryAssignments = new ArrayList<>();
    private Double currentJuryScore;
    private LocalDateTime currentJuryScoredAt;

    public ApplicationDTO(Application app) {
        this.id = app.getId();
        if (app.getApplicant() != null) {
            this.applicantFullName = app.getApplicant().getFirstName() + " " + app.getApplicant().getLastName();
            this.applicantUsername = app.getApplicant().getUsername();
        } else {
            this.applicantFullName = "Bilinmeyen Kullanıcı";
            this.applicantUsername = "N/A";
        }
        this.tytScore = app.getTytScore();
        this.obp = app.getObp();
        this.faculty = app.getFaculty();
        this.departmentId = app.getDepartment() != null ? app.getDepartment().getId() : null;
        this.programName = app.getDepartment() != null ? app.getDepartment().getName() : app.getProgramName();
        this.performancePreferences = app.getPerformancePreferences();
        this.status = app.getStatus() != null ? app.getStatus().name() : "BELIRSIZ";
        this.oyspScore = app.getOyspScore();
        this.standardizedOyspScore = app.getStandardizedOyspScore();
        this.finalPlacementScore = app.getFinalPlacementScore();
        this.placementRank = app.getPlacementRank();
        this.resultPublished = app.getResultPublishedAt() != null;
        this.placementStatus = resultPublished && app.getPlacementStatus() != null
                ? app.getPlacementStatus().name() : null;
        this.osymDocPath = app.getOsymDocPath();
        this.diplomaDocPath = app.getDiplomaDocPath();
        this.healthDocPath = app.getHealthDocPath();
        this.photoDocPath = app.getPhotoDocPath();
        this.nationalDocPath = app.getNationalDocPath();
        this.disabledDocPath = app.getDisabledDocPath();
        this.isNational = app.getIsNational();
        this.isDisabled = app.getIsDisabled();
        if (app.getExamSession() != null) {
            this.examSessionType = app.getExamSession().getSessionType().name();
            this.examDate = app.getExamSession().getExamDate();
            this.examStartTime = app.getExamSession().getStartTime();
            this.examEndTime = app.getExamSession().getEndTime();
            this.examLocation = app.getExamSession().getLocation();
            this.examRoom = app.getExamSession().getRoom();
            this.examSchedulePublished = app.getExamSession().isPublished();
        } else {
            this.examSessionType = null;
            this.examDate = null;
            this.examStartTime = null;
            this.examEndTime = null;
            this.examLocation = null;
            this.examRoom = null;
            this.examSchedulePublished = false;
        }
        this.examOrder = app.getExamOrder();
        this.appointmentStartTime = app.getAppointmentStartTime();
        this.appointmentEndTime = app.getAppointmentEndTime();
    }

    public Long getId() { return id; }
    public String getApplicantFullName() { return applicantFullName; }
    public String getApplicantUsername() { return applicantUsername; }
    public Double getTytScore() { return tytScore; }
    public Double getObp() { return obp; }
    public String getFaculty() { return faculty; }
    public Long getDepartmentId() { return departmentId; }
    public String getProgramName() { return programName; }
    public String getPerformancePreferences() { return performancePreferences; }
    public String getStatus() { return status; }
    public Double getOyspScore() { return oyspScore; }
    public Double getStandardizedOyspScore() { return standardizedOyspScore; }
    public Double getFinalPlacementScore() { return finalPlacementScore; }
    public Integer getPlacementRank() { return placementRank; }
    public String getPlacementStatus() { return placementStatus; }
    public boolean isResultPublished() { return resultPublished; }
    public String getOsymDocPath() { return osymDocPath; }
    public String getDiplomaDocPath() { return diplomaDocPath; }
    public String getHealthDocPath() { return healthDocPath; }
    public String getPhotoDocPath() { return photoDocPath; }
    public String getNationalDocPath() { return nationalDocPath; }
    public String getDisabledDocPath() { return disabledDocPath; }
    public Boolean getIsNational() { return isNational; }
    public Boolean getIsDisabled() { return isDisabled; }
    public String getExamSessionType() { return examSessionType; }
    public LocalDate getExamDate() { return examDate; }
    public LocalTime getExamStartTime() { return examStartTime; }
    public LocalTime getExamEndTime() { return examEndTime; }
    public String getExamLocation() { return examLocation; }
    public String getExamRoom() { return examRoom; }
    public Integer getExamOrder() { return examOrder; }
    public LocalTime getAppointmentStartTime() { return appointmentStartTime; }
    public LocalTime getAppointmentEndTime() { return appointmentEndTime; }
    public boolean isExamSchedulePublished() { return examSchedulePublished; }
    public List<CandidateJuryAssignmentDTO> getJuryAssignments() { return juryAssignments; }
    public void setJuryAssignments(List<CandidateJuryAssignmentDTO> juryAssignments) { this.juryAssignments = juryAssignments; }
    public Double getCurrentJuryScore() { return currentJuryScore; }
    public void setCurrentJuryScore(Double currentJuryScore) { this.currentJuryScore = currentJuryScore; }
    public LocalDateTime getCurrentJuryScoredAt() { return currentJuryScoredAt; }
    public void setCurrentJuryScoredAt(LocalDateTime currentJuryScoredAt) { this.currentJuryScoredAt = currentJuryScoredAt; }
}
