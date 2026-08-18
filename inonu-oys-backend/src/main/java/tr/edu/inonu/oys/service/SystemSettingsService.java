package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.SystemSettingsDTO;
import tr.edu.inonu.oys.model.SystemSettings;
import tr.edu.inonu.oys.repository.SystemSettingsRepository;

import java.time.LocalDate;

@Service
public class SystemSettingsService {
    private final SystemSettingsRepository repository;

    public SystemSettingsService(SystemSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SystemSettings getOrCreate() {
        return repository.findById(1L).orElseGet(() -> repository.save(new SystemSettings()));
    }

    @Transactional
    public SystemSettingsDTO getSettings() {
        return new SystemSettingsDTO(getOrCreate());
    }

    @Transactional
    public SystemSettingsDTO update(SystemSettingsDTO request) {
        SystemSettings s = getOrCreate();
        s.setApplicationsOpen(request.applicationsOpen);
        s.setApplicationStartDate(request.applicationStartDate);
        s.setApplicationEndDate(request.applicationEndDate);
        s.setMinTytScore(request.minTytScore != null ? request.minTytScore : 150.0);
        s.setRequireObp(request.requireObp);
        s.setRequireOsymDocument(request.requireOsymDocument);
        s.setRequireDiplomaDocument(request.requireDiplomaDocument);
        s.setRequireHealthDocument(request.requireHealthDocument);
        s.setRequirePhotoDocument(request.requirePhotoDocument);
        s.setRequireNationalDocument(request.requireNationalDocument);
        s.setRequireDisabledDocument(request.requireDisabledDocument);
        s.setExamDocumentEnabled(request.examDocumentEnabled);
        s.setResultDocumentEnabled(request.resultDocumentEnabled);
        return new SystemSettingsDTO(repository.save(s));
    }

    public void validateApplicationWindow(SystemSettings s) {
        LocalDate today = LocalDate.now();
        if (!s.isApplicationsOpen()) throw new RuntimeException("Başvuru sistemi şu anda kapalıdır.");
        if (s.getApplicationStartDate() != null && today.isBefore(s.getApplicationStartDate())) {
            throw new RuntimeException("Başvurular henüz başlamadı.");
        }
        if (s.getApplicationEndDate() != null && today.isAfter(s.getApplicationEndDate())) {
            throw new RuntimeException("Başvuru süresi sona erdi.");
        }
    }
}
