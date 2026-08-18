package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.SystemSettings;
import java.time.LocalDate;

public class SystemSettingsDTO {
    public boolean applicationsOpen;
    public LocalDate applicationStartDate;
    public LocalDate applicationEndDate;
    public Double minTytScore;
    public boolean requireObp;
    public boolean requireOsymDocument;
    public boolean requireDiplomaDocument;
    public boolean requireHealthDocument;
    public boolean requirePhotoDocument;
    public boolean requireNationalDocument;
    public boolean requireDisabledDocument;
    public boolean examDocumentEnabled;
    public boolean resultDocumentEnabled;

    public SystemSettingsDTO() {}

    public SystemSettingsDTO(SystemSettings s) {
        this.applicationsOpen = s.isApplicationsOpen();
        this.applicationStartDate = s.getApplicationStartDate();
        this.applicationEndDate = s.getApplicationEndDate();
        this.minTytScore = s.getMinTytScore();
        this.requireObp = s.isRequireObp();
        this.requireOsymDocument = s.isRequireOsymDocument();
        this.requireDiplomaDocument = s.isRequireDiplomaDocument();
        this.requireHealthDocument = s.isRequireHealthDocument();
        this.requirePhotoDocument = s.isRequirePhotoDocument();
        this.requireNationalDocument = s.isRequireNationalDocument();
        this.requireDisabledDocument = s.isRequireDisabledDocument();
        this.examDocumentEnabled = s.isExamDocumentEnabled();
        this.resultDocumentEnabled = s.isResultDocumentEnabled();
    }
}
