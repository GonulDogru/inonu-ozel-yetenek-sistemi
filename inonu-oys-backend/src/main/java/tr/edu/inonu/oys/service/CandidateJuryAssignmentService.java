package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.CandidateJuryAssignmentDTO;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.CandidateJuryAssignmentRepository;
import tr.edu.inonu.oys.repository.UserRepository;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class CandidateJuryAssignmentService {
    private final CandidateJuryAssignmentRepository assignmentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public CandidateJuryAssignmentService(CandidateJuryAssignmentRepository assignmentRepository,
                                          ApplicationRepository applicationRepository,
                                          UserRepository userRepository,
                                          UserService userService) {
        this.assignmentRepository = assignmentRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<CandidateJuryAssignmentDTO> listForApplication(Long applicationId, User currentUser) {
        Application application = getApplication(applicationId);
        requireCanManageApplicationDepartment(application, currentUser);
        return assignmentRepository.findByApplicationIdOrderByMatchScoreDescIdAsc(applicationId)
                .stream()
                .filter(this::isActiveAssignment)
                .map(CandidateJuryAssignmentDTO::new).toList();
    }

    @Transactional
    public List<CandidateJuryAssignmentDTO> suggest(Long applicationId, User currentUser) {
        Application application = getApplication(applicationId);
        requireCanManageApplicationDepartment(application, currentUser);
        if (application.getDepartment() == null) {
            throw new RuntimeException("Başvurunun bölümü bulunamadı.");
        }

        String expectedJuryField = expectedJuryField(application.getDepartment());
        List<CandidateJuryAssignment> existingApproved = assignmentRepository
                .findByApplicationIdOrderByMatchScoreDescIdAsc(applicationId).stream()
                .filter(item -> item.getStatus() == CandidateJuryAssignmentStatus.APPROVED)
                .toList();
        boolean existingApprovedIsValid = existingApproved.size() == 3
                && existingApproved.stream().allMatch(assignment -> assignmentFitsApplication(assignment, application, expectedJuryField));
        if (existingApprovedIsValid) {
            return existingApproved.stream().map(CandidateJuryAssignmentDTO::new).toList();
        }
        existingApproved.forEach(assignment -> {
            assignment.setStatus(CandidateJuryAssignmentStatus.REPLACED);
            assignmentRepository.save(assignment);
        });

        assignmentRepository.deleteByApplicationIdAndStatus(applicationId, CandidateJuryAssignmentStatus.SUGGESTED);

        List<User> candidateJuries = userRepository
                .findActiveJuriesByDepartmentWithDepartments(application.getDepartment().getId())
                .stream()
                .filter(jury -> juryFieldMatches(jury, expectedJuryField))
                .filter(jury -> jurySpecialtyScopeMatches(jury, application.getDepartment()))
                .toList();

        List<CandidateJuryAssignment> suggestions = candidateJuries.stream()
                .distinct()
                .map(jury -> buildSuggestion(application, jury,
                        assignmentRepository.findByApplicationIdAndJuryId(applicationId, jury.getId())
                                .orElseGet(CandidateJuryAssignment::new)))
                .sorted(Comparator.comparing(CandidateJuryAssignment::getMatchScore).reversed()
                        .thenComparing(item -> item.getJury().getLastName())
                        .thenComparing(item -> item.getJury().getFirstName()))
                .limit(3)
                .toList();

        if (suggestions.size() < 3) {
            throw new RuntimeException("Bu aday için önerilecek en az 3 aktif jüri bulunamadı.");
        }

        return assignmentRepository.saveAll(suggestions).stream()
                .map(CandidateJuryAssignmentDTO::new)
                .toList();
    }

    @Transactional
    public List<CandidateJuryAssignmentDTO> approve(Long applicationId, List<Long> juryIds, User currentUser) {
        Application application = getApplication(applicationId);
        requireCanManageApplicationDepartment(application, currentUser);
        if (juryIds == null || juryIds.size() != 3) {
            throw new RuntimeException("Bir aday için tam 3 jüri onaylanmalıdır.");
        }

        assignmentRepository.findByApplicationIdOrderByMatchScoreDescIdAsc(applicationId).forEach(existing -> {
            if (existing.getStatus() == CandidateJuryAssignmentStatus.APPROVED) {
                existing.setStatus(CandidateJuryAssignmentStatus.REPLACED);
                assignmentRepository.save(existing);
            }
        });

        List<CandidateJuryAssignment> approved = juryIds.stream().distinct().map(juryId -> {
            User jury = userRepository.findByIdWithDepartments(juryId)
                    .orElseThrow(() -> new RuntimeException("Jüri bulunamadı: " + juryId));
            boolean assignedToDepartment = jury.getAssignedDepartments().stream()
                    .anyMatch(department -> application.getDepartment() != null
                            && department.getId().equals(application.getDepartment().getId()));
            if (!assignedToDepartment || jury.getRole() != Role.JURY || !jury.isActive()
                    || !juryFieldMatches(jury, expectedJuryField(application.getDepartment()))
                    || !jurySpecialtyScopeMatches(jury, application.getDepartment())) {
                throw new RuntimeException(jury.getFirstName() + " " + jury.getLastName() + " bu adayın bölümü için aktif jüri değil.");
            }
            CandidateJuryAssignment assignment = assignmentRepository.findByApplicationIdAndJuryId(applicationId, juryId)
                    .orElseGet(CandidateJuryAssignment::new);
            assignment.setApplication(application);
            assignment.setJury(jury);
            CandidateJuryAssignment scored = buildSuggestion(application, jury);
            assignment.setMatchScore(scored.getMatchScore());
            assignment.setMatchedAreas(scored.getMatchedAreas());
            assignment.setStatus(CandidateJuryAssignmentStatus.APPROVED);
            assignment.setApprovedAt(LocalDateTime.now());
            assignment.setApprovedBy(currentUser);
            return assignment;
        }).toList();

        if (approved.size() != 3) {
            throw new RuntimeException("Aynı jüri birden fazla seçilemez. Tam 3 farklı jüri seçin.");
        }

        return assignmentRepository.saveAll(approved).stream()
                .map(CandidateJuryAssignmentDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isApprovedJury(Long applicationId, Long juryId) {
        return assignmentRepository.existsByApplicationIdAndJuryIdAndStatus(
                applicationId, juryId, CandidateJuryAssignmentStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Application> approvedApplicationsForJury(Long juryId) {
        return assignmentRepository
                .findByJuryIdAndStatusOrderByApplicationApplicantLastNameAscApplicationApplicantFirstNameAsc(
                        juryId, CandidateJuryAssignmentStatus.APPROVED)
                .stream().map(CandidateJuryAssignment::getApplication).toList();
    }

    @Transactional(readOnly = true)
    public List<CandidateJuryAssignmentDTO> approvedForApplication(Long applicationId) {
        return assignmentRepository.findByApplicationIdOrderByMatchScoreDescIdAsc(applicationId)
                .stream()
                .filter(assignment -> assignment.getStatus() == CandidateJuryAssignmentStatus.APPROVED)
                .map(CandidateJuryAssignmentDTO::new)
                .toList();
    }

    private CandidateJuryAssignment buildSuggestion(Application application, User jury) {
        return buildSuggestion(application, jury, new CandidateJuryAssignment());
    }

    private CandidateJuryAssignment buildSuggestion(Application application, User jury, CandidateJuryAssignment assignment) {
        assignment.setApplication(application);
        assignment.setJury(jury);
        List<String> matched = matchedAreas(application.getPerformancePreferences(), jury.getJurySpecialties());
        int score = matched.isEmpty() ? fallbackScore(application) : Math.min(100, 50 + matched.size() * 25);
        assignment.setMatchScore(score);
        assignment.setMatchedAreas(String.join(", ", matched.isEmpty() ? List.of("Genel bölüm jüri eşleşmesi") : matched));
        assignment.setStatus(CandidateJuryAssignmentStatus.SUGGESTED);
        return assignment;
    }

    private boolean isActiveAssignment(CandidateJuryAssignment assignment) {
        return assignment.getStatus() == CandidateJuryAssignmentStatus.SUGGESTED
                || assignment.getStatus() == CandidateJuryAssignmentStatus.APPROVED;
    }

    private int fallbackScore(Application application) {
        String prefs = normalize(application.getPerformancePreferences());
        if (prefs.contains("ortak parkur") || prefs.contains("cizim sinavi")) return 70;
        return 35;
    }

    private String expectedJuryField(Department department) {
        String scope = departmentScope(department);
        return "SPORT".equals(scope) ? "SPOR" : scope;
    }

    private boolean juryFieldMatches(User jury, String expectedJuryField) {
        return expectedJuryField == null || expectedJuryField.equals(jury.getJuryField());
    }

    private boolean assignmentFitsApplication(CandidateJuryAssignment assignment, Application application, String expectedJuryField) {
        User jury = assignment.getJury();
        if (jury == null || application.getDepartment() == null) return false;
        boolean assignedToDepartment = jury.getAssignedDepartments().stream()
                .anyMatch(department -> department.getId().equals(application.getDepartment().getId()));
        return assignedToDepartment
                && jury.getRole() == Role.JURY
                && jury.isActive()
                && juryFieldMatches(jury, expectedJuryField)
                && jurySpecialtyScopeMatches(jury, application.getDepartment());
    }

    private boolean jurySpecialtyScopeMatches(User jury, Department department) {
        String scope = departmentScope(department);
        if ("GENERAL".equals(scope)) return true;
        String specialties = normalize(jury.getJurySpecialties());
        if (specialties.isBlank()) return false;
        return switch (scope) {
            case "MUSIC" -> containsAny(specialties, "enstruman", "san", "vokal", "ritim", "melodi", "isitme", "kulak", "baglama", "gitar", "piyano", "keman");
            case "ART" -> containsAny(specialties, "cizim", "desen", "gozlem", "kompozisyon", "oran");
            case "CERAMIC" -> containsAny(specialties, "seramik", "sekillendirme", "modelleme", "hacim", "tasarim");
            case "SPORT" -> containsAny(specialties, "ortak parkur", "sprint", "slalom", "engel", "denge", "parkur");
            default -> true;
        };
    }

    private String departmentScope(Department department) {
        String name = normalize(department != null ? department.getName() : "");
        if (containsAny(name, "beden", "antrenor", "spor yoneticiligi", "engelliler")) return "SPORT";
        if (containsAny(name, "muzik", "muzikoloji")) return "MUSIC";
        if (name.contains("resim")) return "ART";
        if (name.contains("seramik")) return "CERAMIC";
        return "GENERAL";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private List<String> matchedAreas(String performancePreferences, String jurySpecialties) {
        if (performancePreferences == null || performancePreferences.isBlank()) return List.of();
        String normalizedPrefs = normalize(performancePreferences);
        return parseSpecialties(jurySpecialties).stream()
                .filter(specialty -> specialtyMatches(normalizedPrefs, specialty))
                .toList();
    }

    private boolean specialtyMatches(String normalizedPreferences, String specialty) {
        String normalizedSpecialty = normalize(specialty);
        if (normalizedSpecialty.contains(":")) {
            String[] parts = normalizedSpecialty.split(":", 2);
            return normalizedPreferences.contains(parts[0].trim())
                    && normalizedPreferences.contains(parts[1].trim());
        }
        return normalizedPreferences.contains(normalizedSpecialty);
    }

    private List<String> parseSpecialties(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return java.util.Arrays.stream(raw.replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String normalize(String value) {
        String lower = value == null ? "" : value.toLowerCase(Locale.forLanguageTag("tr-TR"));
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("ı", "i");
    }

    private Application getApplication(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı."));
    }

    private void requireCanManageApplicationDepartment(Application application, User currentUser) {
        if (application.getDepartment() == null) {
            throw new RuntimeException("Başvurunun bölüm bilgisi bulunamadı.");
        }
        userService.requireCanManageDepartment(currentUser, application.getDepartment().getId());
    }
}
