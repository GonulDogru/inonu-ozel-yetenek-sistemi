package tr.edu.inonu.oys.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "applications")
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-applications")
    private User applicant;

    private Double tytScore;
    private Double obp;
    private String faculty;
    // Legacy display value is retained during the department_id migration.
    private String programName;
    @Column(columnDefinition = "TEXT")
    private String performancePreferences;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    private Boolean isNational;
    private Boolean isDisabled;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Double oyspScore; 
    private Double finalPlacementScore; 
    private Double averageScore; // YENİ EKLENDİ
    private Double standardizedOyspScore;
    private Integer placementRank;

    @Enumerated(EnumType.STRING)
    private PlacementStatus placementStatus;
    private LocalDateTime resultPublishedAt;

    private String osymDocPath;
    private String diplomaDocPath;
    private String healthDocPath;
    private String photoDocPath;
    private String nationalDocPath;
    private String disabledDocPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id")
    private ExamSession examSession;
    private Integer examOrder;
    private LocalTime appointmentStartTime;
    private LocalTime appointmentEndTime;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY)
    @JsonManagedReference("application-scores")
    private List<JuryScore> juryScores;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getApplicant() { return applicant; }
    public void setApplicant(User applicant) { this.applicant = applicant; }
    public Double getTytScore() { return tytScore; }
    public void setTytScore(Double tytScore) { this.tytScore = tytScore; }
    public Double getObp() { return obp; }
    public void setObp(Double obp) { this.obp = obp; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public String getPerformancePreferences() { return performancePreferences; }
    public void setPerformancePreferences(String performancePreferences) { this.performancePreferences = performancePreferences; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) {
        this.department = department;
        this.programName = department == null ? this.programName : department.getName();
    }
    public Boolean getIsNational() { return isNational; }
    public void setIsNational(Boolean isNational) { this.isNational = isNational; }
    public Boolean getIsDisabled() { return isDisabled; }
    public void setIsDisabled(Boolean isDisabled) { this.isDisabled = isDisabled; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public Double getOyspScore() { return oyspScore; }
    public void setOyspScore(Double oyspScore) { this.oyspScore = oyspScore; }
    public Double getFinalPlacementScore() { return finalPlacementScore; }
    public void setFinalPlacementScore(Double finalPlacementScore) { this.finalPlacementScore = finalPlacementScore; }
    public Double getAverageScore() { return averageScore; } // YENİ EKLENDİ
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; } // YENİ EKLENDİ
    public Double getStandardizedOyspScore() { return standardizedOyspScore; }
    public void setStandardizedOyspScore(Double standardizedOyspScore) { this.standardizedOyspScore = standardizedOyspScore; }
    public Integer getPlacementRank() { return placementRank; }
    public void setPlacementRank(Integer placementRank) { this.placementRank = placementRank; }
    public PlacementStatus getPlacementStatus() { return placementStatus; }
    public void setPlacementStatus(PlacementStatus placementStatus) { this.placementStatus = placementStatus; }
    public LocalDateTime getResultPublishedAt() { return resultPublishedAt; }
    public void setResultPublishedAt(LocalDateTime resultPublishedAt) { this.resultPublishedAt = resultPublishedAt; }
    public String getOsymDocPath() { return osymDocPath; }
    public void setOsymDocPath(String osymDocPath) { this.osymDocPath = osymDocPath; }
    public String getDiplomaDocPath() { return diplomaDocPath; }
    public void setDiplomaDocPath(String diplomaDocPath) { this.diplomaDocPath = diplomaDocPath; }
    public String getHealthDocPath() { return healthDocPath; }
    public void setHealthDocPath(String healthDocPath) { this.healthDocPath = healthDocPath; }
    public String getPhotoDocPath() { return photoDocPath; }
    public void setPhotoDocPath(String photoDocPath) { this.photoDocPath = photoDocPath; }
    public String getNationalDocPath() { return nationalDocPath; }
    public void setNationalDocPath(String nationalDocPath) { this.nationalDocPath = nationalDocPath; }
    public String getDisabledDocPath() { return disabledDocPath; }
    public void setDisabledDocPath(String disabledDocPath) { this.disabledDocPath = disabledDocPath; }
    public ExamSession getExamSession() { return examSession; }
    public void setExamSession(ExamSession examSession) { this.examSession = examSession; }
    public Integer getExamOrder() { return examOrder; }
    public void setExamOrder(Integer examOrder) { this.examOrder = examOrder; }
    public LocalTime getAppointmentStartTime() { return appointmentStartTime; }
    public void setAppointmentStartTime(LocalTime appointmentStartTime) { this.appointmentStartTime = appointmentStartTime; }
    public LocalTime getAppointmentEndTime() { return appointmentEndTime; }
    public void setAppointmentEndTime(LocalTime appointmentEndTime) { this.appointmentEndTime = appointmentEndTime; }
    public List<JuryScore> getJuryScores() { return juryScores; }
    public void setJuryScores(List<JuryScore> juryScores) { this.juryScores = juryScores; }
}
