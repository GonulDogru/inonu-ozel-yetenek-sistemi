package tr.edu.inonu.oys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.CandidateJuryAssignmentRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.UserRepository;
import tr.edu.inonu.oys.dto.CandidateJuryAssignmentDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DepartmentRepository departmentRepository;
    private final SystemSettingsService systemSettingsService;
    private final CandidateJuryAssignmentRepository candidateJuryAssignmentRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository,
                              FileStorageService fileStorageService, DepartmentRepository departmentRepository,
                              SystemSettingsService systemSettingsService,
                              CandidateJuryAssignmentRepository candidateJuryAssignmentRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.departmentRepository = departmentRepository;
        this.systemSettingsService = systemSettingsService;
        this.candidateJuryAssignmentRepository = candidateJuryAssignmentRepository;
    }

    @Transactional
    public Application createApplication(String username, Double tytScore, Double obp, String faculty, Long departmentId,
                                         String performancePreferences,
                                         Boolean isNational, Boolean isDisabled, MultipartFile osymDoc,
                                         MultipartFile diplomaDoc, MultipartFile healthDoc, MultipartFile photoDoc,
                                         MultipartFile nationalDoc, MultipartFile disabledDoc) {
        User applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Ba?vuru yapan kullan?c? sistemde bulunamad?: " + username));
        SystemSettings settings = systemSettingsService.getOrCreate();
        systemSettingsService.validateApplicationWindow(settings);

        if (applicationRepository.existsByApplicantUsername(username)) {
            throw new RuntimeException("Bu T.C. Kimlik Numaras? ile zaten aktif bir ba?vurunuz bulunmaktad?r!");
        }
        double minTytScore = settings.getMinTytScore() != null ? settings.getMinTytScore() : 150.0;
        if (tytScore == null || tytScore < minTytScore || tytScore > 500) {
            throw new RuntimeException("TYT puan? " + minTytScore + " ile 500 aras?nda olmal?d?r.");
        }
        if (settings.isRequireObp() && obp == null) {
            throw new RuntimeException("OBP bilgisi zorunludur.");
        }
        if (obp != null && (obp < 0 || obp > 500)) {
            throw new RuntimeException("OBP 0 ile 500 aras?nda olmal?d?r.");
        }
        if (departmentId == null) throw new RuntimeException("B?l?m se?imi zorunludur.");
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Se?ilen b?l?m bulunamad?."));
        if (!department.isTalentAdmissionEnabled()) {
            throw new RuntimeException("Seçilen bölüm özel yetenek başvurusuna açık değildir.");
        }
        if (tytScore < department.getBaseScoreRequirement()) {
            throw new RuntimeException("TYT puan? b?l?m taban puan?n?n alt?nda.");
        }
        validateFaculty(faculty, department);
        validateFiles(settings, isNational, isDisabled, osymDoc, diplomaDoc, healthDoc, photoDoc, nationalDoc, disabledDoc);

        Application application = new Application();
        application.setApplicant(applicant);
        application.setTytScore(tytScore);
        application.setObp(obp);
        application.setFaculty(faculty);
        application.setDepartment(department);
        application.setPerformancePreferences(resolvePerformancePreferences(department, performancePreferences));
        application.setIsNational(isNational);
        application.setIsDisabled(isDisabled);
        application.setStatus(ApplicationStatus.SUBMITTED);

        if (osymDoc != null) application.setOsymDocPath(fileStorageService.storeFile(osymDoc, username, "osym"));
        if (diplomaDoc != null) application.setDiplomaDocPath(fileStorageService.storeFile(diplomaDoc, username, "diploma"));
        if (healthDoc != null) application.setHealthDocPath(fileStorageService.storeFile(healthDoc, username, "saglik"));
        if (photoDoc != null) application.setPhotoDocPath(fileStorageService.storeFile(photoDoc, username, "foto"));
        if (nationalDoc != null) application.setNationalDocPath(fileStorageService.storeFile(nationalDoc, username, "milli"));
        if (disabledDoc != null) application.setDisabledDocPath(fileStorageService.storeFile(disabledDoc, username, "engelli"));

        return applicationRepository.save(application);
    }

    private void validateFiles(SystemSettings settings, Boolean isNational, Boolean isDisabled, MultipartFile osymDoc,
                               MultipartFile diplomaDoc, MultipartFile healthDoc, MultipartFile photoDoc,
                               MultipartFile nationalDoc, MultipartFile disabledDoc) {
        if (settings.isRequireOsymDocument() || (osymDoc != null && !osymDoc.isEmpty())) fileStorageService.validateDocument(osymDoc, "?SYM sonu? belgesi", false);
        if (settings.isRequireDiplomaDocument() || (diplomaDoc != null && !diplomaDoc.isEmpty())) fileStorageService.validateDocument(diplomaDoc, "Diploma belgesi", false);
        if (settings.isRequireHealthDocument() || (healthDoc != null && !healthDoc.isEmpty())) fileStorageService.validateDocument(healthDoc, "Sa?l?k raporu", false);
        if (settings.isRequirePhotoDocument() || (photoDoc != null && !photoDoc.isEmpty())) fileStorageService.validateDocument(photoDoc, "Biyometrik foto?raf", true);
        if (Boolean.TRUE.equals(isNational) && settings.isRequireNationalDocument()) {
            fileStorageService.validateDocument(nationalDoc, "Milli sporcu belgesi", false);
        } else if (nationalDoc != null && !nationalDoc.isEmpty()) {
            fileStorageService.validateDocument(nationalDoc, "Milli sporcu belgesi", false);
        }
        if (Boolean.TRUE.equals(isDisabled) && settings.isRequireDisabledDocument()) {
            fileStorageService.validateDocument(disabledDoc, "Engelli sa?l?k raporu", false);
        } else if (disabledDoc != null && !disabledDoc.isEmpty()) {
            fileStorageService.validateDocument(disabledDoc, "Engelli sa?l?k raporu", false);
        }
    }

    private String resolvePerformancePreferences(Department department, String performancePreferences) {
        if (performancePreferences != null && !performancePreferences.isBlank()) {
            return performancePreferences;
        }
        if (isSportDepartment(department)) {
            return "{\"departmentName\":\"" + department.getName() + "\",\"mode\":\"AUTO\",\"examFormat\":\"Ortak Parkur Performansı\","
                    + "\"selections\":[{\"type\":\"Ortak Parkur Performansı\",\"detail\":\"Sprint, slalom, engel geçme, takla, denge, sağlık topu taşıma, basketbol topu sürme, futbol slalom ve hedefe top atma kriterleriyle değerlendirilir.\",\"otherDetail\":\"\"}],"
                    + "\"legacySelections\":[\"Ortak Parkur Performansı\"]}";
        }
        if (isDrawingOnlyDepartment(department)) {
            return "{\"departmentName\":\"" + department.getName() + "\",\"mode\":\"AUTO\",\"examFormat\":\"Çizim Sınavı\","
                    + "\"selections\":[{\"type\":\"Çizim Sınavı\",\"detail\":\"Çizim becerisi, gözlem, oran-orantı, kompozisyon, ışık-gölge ve yaratıcılık kriterleriyle değerlendirilir.\",\"otherDetail\":\"\"}],"
                    + "\"legacySelections\":[\"Çizim Sınavı\"]}";
        }
        return performancePreferences;
    }

    private boolean isSportDepartment(Department department) {
        String name = department.getName() == null ? "" : department.getName();
        return List.of("Beden", "Antrenörlük", "Spor Yöneticiliği", "Engellilerde")
                .stream().anyMatch(name::contains);
    }

    private boolean isDrawingOnlyDepartment(Department department) {
        String name = department.getName() == null ? "" : department.getName();
        return name.contains("Resim");
    }

    private void validateFaculty(String faculty, Department department) {
        if (!"SPOR".equals(faculty) && !"GSF".equals(faculty)) {
            throw new RuntimeException("Geçersiz fakülte seçimi.");
        }
        boolean sportDepartment = List.of("Beden", "Antrenörlük", "Spor Yöneticiliği", "Engellilerde")
                .stream().anyMatch(department.getName()::contains);
        if (("SPOR".equals(faculty)) != sportDepartment) {
            throw new RuntimeException("Seçilen bölüm fakülteyle uyuşmuyor.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<ApplicationDTO> getApplicationByUsername(String username) {
        return applicationRepository.findByApplicantUsername(username).map(this::toDtoWithAssignments);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAllApplicationsForAdmin() {
        List<Application> applications = applicationRepository.findByApplicantRole(Role.APPLICANT);

        for (Application app : applications) {
            // Eğer ortalama henüz hesaplanmadıysa ve 3 veya daha fazla jüri puan vermişse hesapla
            if (app.getAverageScore() == null && app.getJuryScores() != null && app.getJuryScores().size() >= 3) {
                double avg = app.getJuryScores().stream()
                        .filter(js -> js != null && js.getScore() > 0) // getScore() null olamaz (primitive double)
                        .mapToDouble(JuryScore::getScore)
                        .average()
                        .orElse(0.0);

                if (avg > 0) {
                    double roundedAvg = Math.round(avg * 100.0) / 100.0;
                    app.setAverageScore(roundedAvg);
                    app.setFinalPlacementScore(roundedAvg); // Final skoru da burada set ediliyor
                    // Bu değişiklikler readOnly transaction içinde veritabanına yazılmaz,
                    // ama DTO'ya doğru yansır. Kalıcı olması için ayrı bir @Transactional metot gerekir.
                    // Şimdilik DTO için bu yeterli.
                }
            }
        }
        
        return applications.stream()
                .map(this::toDtoWithAssignments)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAllApplicationsForAdmin(User currentUser) {
        List<ApplicationDTO> applications = getAllApplicationsForAdmin();
        if (currentUser == null || currentUser.getRole() != Role.DEPARTMENT_ADMIN) {
            return applications;
        }
        List<Long> allowedDepartmentIds = currentUser.getAssignedDepartments().stream()
                .map(Department::getId)
                .toList();
        return applications.stream()
                .filter(application -> application.getDepartmentId() != null
                        && allowedDepartmentIds.contains(application.getDepartmentId()))
                .toList();
    }

    @Transactional
    public Application updateApplicationStatus(Long applicationId, String newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı! ID: " + applicationId));

        try {
            ApplicationStatus statusEnum = ApplicationStatus.valueOf(newStatus.toUpperCase());
            boolean validTransition =
                    application.getStatus() == ApplicationStatus.SUBMITTED
                            && (statusEnum == ApplicationStatus.PENDING_EVALUATION
                                || statusEnum == ApplicationStatus.REJECTED);
            if (!validTransition) {
                throw new RuntimeException("Geçersiz başvuru durum geçişi.");
            }
            application.setStatus(statusEnum);
            return applicationRepository.save(application);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz durum değeri: " + newStatus);
        }
    }

    @Transactional
    public ApplicationDTO updateObp(Long applicationId, Double obp) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı."));
        application.setObp(obp);
        return toDtoWithAssignments(applicationRepository.save(application));
    }

    private ApplicationDTO toDtoWithAssignments(Application application) {
        ApplicationDTO dto = new ApplicationDTO(application);
        dto.setJuryAssignments(candidateJuryAssignmentRepository
                .findByApplicationIdOrderByMatchScoreDescIdAsc(application.getId())
                .stream()
                .filter(assignment -> assignment.getStatus() == CandidateJuryAssignmentStatus.APPROVED)
                .map(CandidateJuryAssignmentDTO::new)
                .toList());
        return dto;
    }

    @Transactional(readOnly = true)
    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı"));
    }
}
