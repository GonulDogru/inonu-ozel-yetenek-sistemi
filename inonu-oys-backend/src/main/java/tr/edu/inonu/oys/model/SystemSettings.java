package tr.edu.inonu.oys.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "system_settings")
public class SystemSettings {
    @Id
    private Long id = 1L;

    private boolean applicationsOpen = true;
    private LocalDate applicationStartDate;
    private LocalDate applicationEndDate;
    private Double minTytScore = 150.0;
    private boolean requireObp = false;
    private boolean requireOsymDocument = true;
    private boolean requireDiplomaDocument = true;
    private boolean requireHealthDocument = true;
    private boolean requirePhotoDocument = true;
    private boolean requireNationalDocument = true;
    private boolean requireDisabledDocument = true;
    private boolean examDocumentEnabled = true;
    private boolean resultDocumentEnabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isApplicationsOpen() { return applicationsOpen; }
    public void setApplicationsOpen(boolean applicationsOpen) { this.applicationsOpen = applicationsOpen; }
    public LocalDate getApplicationStartDate() { return applicationStartDate; }
    public void setApplicationStartDate(LocalDate applicationStartDate) { this.applicationStartDate = applicationStartDate; }
    public LocalDate getApplicationEndDate() { return applicationEndDate; }
    public void setApplicationEndDate(LocalDate applicationEndDate) { this.applicationEndDate = applicationEndDate; }
    public Double getMinTytScore() { return minTytScore; }
    public void setMinTytScore(Double minTytScore) { this.minTytScore = minTytScore; }
    public boolean isRequireObp() { return requireObp; }
    public void setRequireObp(boolean requireObp) { this.requireObp = requireObp; }
    public boolean isRequireOsymDocument() { return requireOsymDocument; }
    public void setRequireOsymDocument(boolean requireOsymDocument) { this.requireOsymDocument = requireOsymDocument; }
    public boolean isRequireDiplomaDocument() { return requireDiplomaDocument; }
    public void setRequireDiplomaDocument(boolean requireDiplomaDocument) { this.requireDiplomaDocument = requireDiplomaDocument; }
    public boolean isRequireHealthDocument() { return requireHealthDocument; }
    public void setRequireHealthDocument(boolean requireHealthDocument) { this.requireHealthDocument = requireHealthDocument; }
    public boolean isRequirePhotoDocument() { return requirePhotoDocument; }
    public void setRequirePhotoDocument(boolean requirePhotoDocument) { this.requirePhotoDocument = requirePhotoDocument; }
    public boolean isRequireNationalDocument() { return requireNationalDocument; }
    public void setRequireNationalDocument(boolean requireNationalDocument) { this.requireNationalDocument = requireNationalDocument; }
    public boolean isRequireDisabledDocument() { return requireDisabledDocument; }
    public void setRequireDisabledDocument(boolean requireDisabledDocument) { this.requireDisabledDocument = requireDisabledDocument; }
    public boolean isExamDocumentEnabled() { return examDocumentEnabled; }
    public void setExamDocumentEnabled(boolean examDocumentEnabled) { this.examDocumentEnabled = examDocumentEnabled; }
    public boolean isResultDocumentEnabled() { return resultDocumentEnabled; }
    public void setResultDocumentEnabled(boolean resultDocumentEnabled) { this.resultDocumentEnabled = resultDocumentEnabled; }
}
