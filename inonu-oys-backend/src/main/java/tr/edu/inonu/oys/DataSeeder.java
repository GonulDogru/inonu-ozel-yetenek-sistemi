package tr.edu.inonu.oys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.*;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      ClassroomRepository classroomRepository,
                      ApplicationRepository applicationRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.bootstrap.enabled}") boolean enabled,
                      @Value("${app.bootstrap.admin-username}") String adminUsername,
                      @Value("${app.bootstrap.admin-password}") String adminPassword) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.classroomRepository = classroomRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        List<Department> departments = seedDepartments();
        seedUsers(departments);
        seedClassrooms(departments);
        seedApplications(departments);
    }

    private List<Department> seedDepartments() {
        List<Department> existingDepartments = departmentRepository.findAll();
        if (!existingDepartments.isEmpty()) {
            existingDepartments.forEach(this::normalizeDepartmentDefaults);
            return departmentRepository.findAll();
        }

        List<String> names = List.of(
                "Müzik Öğretmenliği",
                "Beden Eğitimi ve Spor Öğretmenliği",
                "Antrenörlük Eğitimi",
                "Spor Yöneticiliği",
                "Engellilerde Egzersiz ve Spor Eğitimi",
                "Resim-İş Öğretmenliği",
                "Grafik Tasarımı",
                "Müzik Bilimleri (Müzikoloji)",
                "Seramik Bölümü"
        );
        names.forEach(name -> {
            Department department = departmentRepository.findByName(name).orElseGet(Department::new);
            department.setName(name);
            normalizeDepartmentDefaults(department);
        });
        return departmentRepository.findAll();
    }

    private void normalizeDepartmentDefaults(Department department) {
        if (department.getCode() == null || department.getCode().isBlank()) {
            department.setCode(codeFor(department.getName()));
        }
        boolean grafik = normalizeText(department.getName()).contains("GRAFIK")
                || "GRAFIK_TASARIMI".equals(department.getCode());
        department.setTalentAdmissionEnabled(!grafik);
        department.setQuota(grafik ? 0 : 30);
        department.setBaseScoreRequirement(150);
        department.setTrimScores(false);

        ExamType examType = examTypeFor(department.getName());
        department.setExamType(examType);
        if (examType == ExamType.TRACK) {
            department.setDefaultCandidateIntervalMinutes(5);
            department.setDefaultSessionDurationMinutes(null);
        } else if (examType == ExamType.GROUP) {
            department.setDefaultCandidateIntervalMinutes(null);
            department.setDefaultSessionDurationMinutes(120);
        } else {
            department.setDefaultCandidateIntervalMinutes(20);
            department.setDefaultSessionDurationMinutes(null);
        }
        department.setDefaultBreakMinutes(5);
        department.setRequiredPrimaryJuryCount(3);
        department.setRequiredBackupJuryCount(2);
        department.setJurySelfInactiveDeadlineHours(24);
        departmentRepository.save(department);
    }

    private ExamType examTypeFor(String name) {
        String normalized = normalizeText(name);
        if (normalized.contains("SPOR") || normalized.contains("ANTRENOR") || normalized.contains("EGZERSIZ")
                || normalized.contains("BEDEN")) {
            return ExamType.TRACK;
        }
        if (normalized.contains("RESIM") || normalized.contains("GRAFIK") || normalized.contains("SERAMIK")) {
            return ExamType.GROUP;
        }
        return ExamType.INDIVIDUAL;
    }

    private void seedUsers(List<Department> departments) {
        User superAdmin = adminUser(adminUsername, adminPassword);

        User musicAdmin = user("20000000002", "Admin1234!", "Müzik", "Admin", Role.DEPARTMENT_ADMIN, null);
        User sportsAdmin = user("20000000004", "Admin1234!", "Spor", "Admin", Role.DEPARTMENT_ADMIN, null);
        User artAdmin = user("20000000006", "Admin1234!", "Sanat", "Admin", Role.DEPARTMENT_ADMIN, null);
        resetDepartmentAssignments(musicAdmin);
        resetDepartmentAssignments(sportsAdmin);
        resetDepartmentAssignments(artAdmin);
        departments.stream().filter(this::isMusicDepartment)
                .forEach(department -> assignDepartment(musicAdmin, department, JuryAssignmentRole.PRIMARY));
        departments.stream().filter(this::isSportsDepartment)
                .forEach(department -> assignDepartment(sportsAdmin, department, JuryAssignmentRole.PRIMARY));
        departments.stream().filter(this::isArtDepartment)
                .forEach(department -> assignDepartment(artAdmin, department, JuryAssignmentRole.PRIMARY));

        int usernameSeed = 300000000;
        for (Department department : departments) {
            if (!department.isTalentAdmissionEnabled()) continue;
            for (int i = 1; i <= 3; i++) {
                User jury = user(tckn(usernameSeed++), "Juri1234!", department.getCode() + " Jüri", String.valueOf(i), Role.JURY, fieldFor(department));
                setDefaultJurySpecialties(jury, department);
                assignDepartment(jury, department, JuryAssignmentRole.PRIMARY);
            }
            for (int i = 1; i <= 2; i++) {
                User jury = user(tckn(usernameSeed++), "Juri1234!", department.getCode() + " Jüri", String.valueOf(i + 3), Role.JURY, fieldFor(department));
                setDefaultJurySpecialties(jury, department);
                assignDepartment(jury, department, JuryAssignmentRole.BACKUP);
            }
        }
        seedStableTestJuries(departments);
        normalizeJuryDepartmentAssignments();

        user("40000000002", "Ogrenci1234!", "Ali", "Aday", Role.APPLICANT, null);
        user("40000000004", "Ogrenci1234!", "Ayşe", "Aday", Role.APPLICANT, null);
        user("40000000006", "Ogrenci1234!", "Mehmet", "Aday", Role.APPLICANT, null);
        user("40000000008", "Ogrenci1234!", "Zeynep", "Aday", Role.APPLICANT, null);
        user("40000000010", "Ogrenci1234!", "Deniz", "Aday", Role.APPLICANT, null);
        user("41000000002", "Ogrenci1234!", "Ece", "Test", Role.APPLICANT, null);
        user("41000000004", "Ogrenci1234!", "Mert", "Test", Role.APPLICANT, null);
        user("41000000006", "Ogrenci1234!", "Selin", "Test", Role.APPLICANT, null);
        user("41000000008", "Ogrenci1234!", "Baran", "Test", Role.APPLICANT, null);

        userRepository.save(superAdmin);
    }

    private void seedClassrooms(List<Department> departments) {
        for (Department department : departments) {
            if (!department.isTalentAdmissionEnabled()) continue;
            if (!classroomRepository.findByDepartmentIdOrderByNameAsc(department.getId()).isEmpty()) continue;
            Classroom first = new Classroom();
            first.setDepartment(department);
            first.setName(department.getCode() + " Salon 1");
            first.setBuilding("İnönü Üniversitesi");
            first.setCapacity(department.getExamType() == ExamType.GROUP ? 25 : 1);
            first.setActive(true);
            classroomRepository.save(first);

            Classroom second = new Classroom();
            second.setDepartment(department);
            second.setName(department.getCode() + " Salon 2");
            second.setBuilding("İnönü Üniversitesi");
            second.setCapacity(department.getExamType() == ExamType.GROUP ? 25 : 1);
            second.setActive(true);
            classroomRepository.save(second);
        }
    }

    private void seedApplications(List<Department> departments) {
        List<User> applicants = List.of(
                userRepository.findByUsername("40000000002").orElseThrow(),
                userRepository.findByUsername("40000000004").orElseThrow(),
                userRepository.findByUsername("40000000006").orElseThrow(),
                userRepository.findByUsername("40000000008").orElseThrow(),
                userRepository.findByUsername("40000000010").orElseThrow()
        );
        List<Department> openDepartments = departments.stream()
                .filter(Department::isTalentAdmissionEnabled)
                .toList();
        if (openDepartments.isEmpty()) return;
        for (int i = 0; i < applicants.size(); i++) {
            User applicant = applicants.get(i);
            if (applicationRepository.existsByApplicantUsername(applicant.getUsername())) continue;
            Department department = openDepartments.get(i % openDepartments.size());
            Application application = new Application();
            application.setApplicant(applicant);
            application.setDepartment(department);
            application.setFaculty("Özel Yetenek");
            application.setTytScore(250.0 + i * 10);
            application.setObp(350.0 + i * 5);
            application.setIsNational(false);
            application.setIsDisabled(false);
            application.setStatus(ApplicationStatus.PENDING_EVALUATION);
            applicationRepository.save(application);
        }
    }

    private User user(String username, String password, String firstName, String lastName, Role role, String juryField) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        user.setUsername(username);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setJuryField(juryField);
        return userRepository.save(user);
    }

    private void seedStableTestJuries(List<Department> departments) {
        for (Department department : departments) {
            if (!department.isTalentAdmissionEnabled()) continue;
            long baseUsername = stableJuryBaseFor(department);
            for (int i = 1; i <= 5; i++) {
                String username = String.valueOf(baseUsername + i * 2L);
                User jury = user(username, "Juri1234!", department.getCode() + " Test Jüri", String.valueOf(i), Role.JURY, fieldFor(department));
                jury.setActive(true);
                setDefaultJurySpecialties(jury, department);
                assignDepartment(jury, department, i <= 3 ? JuryAssignmentRole.PRIMARY : JuryAssignmentRole.BACKUP);
            }
        }
    }

    private long stableJuryBaseFor(Department department) {
        String normalized = normalizeText(department.getName());
        if (normalized.contains("MUZIK") && normalized.contains("OGRETMEN")) return 32000000000L;
        if (normalized.contains("MUZIK")) return 32000000100L;
        if (normalized.contains("RESIM")) return 33000000000L;
        if (normalized.contains("SERAMIK")) return 33000000100L;
        if (normalized.contains("BEDEN")) return 34000000000L;
        if (normalized.contains("ANTRENOR")) return 34000000100L;
        if (normalized.contains("YONETICILIGI")) return 34000000200L;
        if (normalized.contains("ENGELLILER")) return 34000000300L;
        return 35000000000L + Math.abs(normalized.hashCode() % 1000) * 100L;
    }

    private void setDefaultJurySpecialties(User jury, Department department) {
        if (jury.getJurySpecialties() != null && !jury.getJurySpecialties().isBlank()) return;
        String normalized = normalizeText(department.getName());
        if (department.getExamType() == ExamType.TRACK) {
            jury.setJurySpecialties("[\"Ortak Parkur Performansı\",\"Sprint\",\"Slalom\",\"Engel geçme\",\"Denge\"]");
        } else if (normalized.contains("MUZIK")) {
            jury.setJurySpecialties("[\"Enstrüman performansı: Piyano\",\"Enstrüman performansı: Bağlama\",\"Ritim tekrarı\",\"Melodi tekrarı\",\"İşitme / kulak sınavı\"]");
        } else if (normalized.contains("RESIM")) {
            jury.setJurySpecialties("[\"Çizim Sınavı\",\"Desen çizimi\",\"Gözlem çizimi\",\"Kompozisyon\"]");
        } else if (normalized.contains("SERAMIK")) {
            jury.setJurySpecialties("[\"Şekillendirme\",\"Modelleme\",\"Hacim algısı\",\"Tasarım\"]");
        }
        userRepository.save(jury);
    }

    private User adminUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        user.setUsername(username);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            user.setFirstName("Sistem");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            user.setLastName("Yöneticisi");
        }
        user.setRole(Role.SUPER_ADMIN);
        user.setJuryField(null);
        return userRepository.save(user);
    }

    private void assignDepartment(User user, Department department, JuryAssignmentRole role) {
        user.getAssignedDepartments().add(department);
        userRepository.save(user);
        userRepository.updateJuryAssignmentRole(user.getId(), department.getId(), role.name());
    }

    private void resetDepartmentAssignments(User user) {
        user.getAssignedDepartments().clear();
        userRepository.save(user);
    }

    private void normalizeJuryDepartmentAssignments() {
        userRepository.findByRoleWithDepartments(Role.JURY).stream().distinct().forEach(jury -> {
            boolean changed = jury.getAssignedDepartments().removeIf(department -> !juryMatchesDepartment(jury, department));
            if (changed) {
                userRepository.save(jury);
            }
        });
    }

    private boolean juryMatchesDepartment(User jury, Department department) {
        String scope = departmentScope(department);
        return expectedJuryFieldForScope(scope).equals(jury.getJuryField());
    }

    private String departmentScope(Department department) {
        String normalized = normalizeText(department.getName());
        if (normalized.contains("MUZIK") || normalized.contains("MUZIKOLOJI")) return "MUSIC";
        if (normalized.contains("RESIM")) return "ART";
        if (normalized.contains("SERAMIK")) return "CERAMIC";
        if (normalized.contains("SPOR") || normalized.contains("ANTRENOR")
                || normalized.contains("BEDEN") || normalized.contains("EGZERSIZ")) return "SPORT";
        return "GENERAL";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private Department firstDepartment(List<Department> departments, ExamType examType) {
        return departments.stream()
                .filter(Department::isTalentAdmissionEnabled)
                .filter(department -> department.getExamType() == examType)
                .findFirst()
                .orElse(departments.get(0));
    }

    private String fieldFor(Department department) {
        return expectedJuryFieldForScope(departmentScope(department));
    }

    private String expectedJuryFieldForScope(String scope) {
        return "SPORT".equals(scope) ? "SPOR" : scope;
    }

    private boolean isMusicDepartment(Department department) {
        String normalized = normalizeText(department.getName());
        return department.isTalentAdmissionEnabled()
                && (normalized.contains("MUZIK") || normalized.contains("MUZIKOLOJI"));
    }

    private boolean isSportsDepartment(Department department) {
        String normalized = normalizeText(department.getName());
        return department.isTalentAdmissionEnabled()
                && (normalized.contains("SPOR") || normalized.contains("ANTRENOR")
                || normalized.contains("BEDEN") || normalized.contains("EGZERSIZ"));
    }

    private boolean isArtDepartment(Department department) {
        String normalized = normalizeText(department.getName());
        return department.isTalentAdmissionEnabled()
                && (normalized.contains("RESIM") || normalized.contains("SERAMIK"));
    }

    private String codeFor(String name) {
        return normalizeText(name)
                .replace(" ", "_")
                .replace("-", "_")
                .replaceAll("[^A-Z0-9_]", "");
    }

    private String normalizeText(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("ı", "i")
                .replace("İ", "I")
                .toUpperCase(Locale.ROOT);
    }

    private String tckn(int seed) {
        String base = String.valueOf(seed);
        while (base.length() < 10) base = "0" + base;
        return base.substring(0, 10) + "2";
    }
}
