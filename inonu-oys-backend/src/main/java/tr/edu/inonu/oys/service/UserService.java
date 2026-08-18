package tr.edu.inonu.oys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.UserDTO;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.UserRepository;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.text.Normalizer;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;

    @Autowired
    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder, ValidationService validationService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }

    public User registerUser(User user) {
        if (!validationService.isValidTCKN(user.getUsername())) {
            throw new RuntimeException("Geçersiz T.C. Kimlik Numarası formatı!");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Bu kullanıcı adı zaten sisteme kayıtlı!");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        if (user.getRole() == null) {
            user.setRole(Role.APPLICANT);
        }
        
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public User loginUser(String username, String password) {
        User user = userRepository.findByUsernameWithDepartments(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (!user.isActive()) {
            throw new RuntimeException("Kullanıcı hesabı pasif durumda!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Geçersiz şifre!");
        }
        return user;
    }

    // METOT GÜNCELLENDİ: Artık "istekli" (eager) veri çeken yeni repository metodunu kullanıyor.
    // Bu, LazyInitializationException'ı kesin olarak çözer.
    public List<UserDTO> findAllJuryMembers() {
        List<User> juries = userRepository.findByRoleWithDepartments(Role.JURY);
        return juries.stream()
                .distinct()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findJuryMembersForAdmin(User currentUser) {
        List<User> juries = userRepository.findByRoleWithDepartments(Role.JURY);
        if (currentUser == null || currentUser.getRole() != Role.DEPARTMENT_ADMIN) {
            return juries.stream().map(this::toUserDto).collect(Collectors.toList());
        }
        User adminWithDepartments = userRepository.findByIdWithDepartments(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Admin kullanıcısı bulunamadı."));
        List<Long> allowedDepartmentIds = assignedDepartmentIds(adminWithDepartments);
        List<Department> allowedDepartments = adminWithDepartments.getAssignedDepartments().stream().toList();
        List<String> allowedScopes = allowedDepartments.stream().map(this::departmentScope).distinct().toList();
        return juries.stream()
                .distinct()
                .filter(jury -> jury.getAssignedDepartments().stream()
                        .anyMatch(department -> allowedDepartmentIds.contains(department.getId())))
                .filter(jury -> allowedScopes.contains(juryOwnScope(jury)))
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllAdminUsers() {
        return userRepository.findByRolesWithDepartments(List.of(Role.ADMIN, Role.DEPARTMENT_ADMIN)).stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional
    public UserDTO setDepartmentAssignment(Long userId, Long departmentId, boolean assigned) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı! ID: " + userId));
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.DEPARTMENT_ADMIN) {
            throw new RuntimeException("Sadece admin kullanıcılarının bölüm yetkisi yönetilebilir.");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı."));

        if (assigned) {
            admin.getAssignedDepartments().add(department);
        } else {
            admin.getAssignedDepartments().removeIf(item -> item.getId().equals(departmentId));
        }

        return toUserDto(userRepository.save(admin));
    }

    private UserDTO toUserDto(User user) {
        UserDTO dto = new UserDTO(user);
        Map<Long, String> roles = new HashMap<>();
        userRepository.findJuryAssignmentRoles(user.getId()).forEach(row -> {
            Number departmentId = (Number) row[0];
            roles.put(departmentId.longValue(), String.valueOf(row[1]));
        });
        if (!roles.isEmpty()) {
            dto.setAssignedDepartmentRoles(roles);
        }
        return dto;
    }

    @Transactional
    public UserDTO deleteUser(Long userId) {
        return setUserActive(userId, false);
    }

    @Transactional
    public UserDTO setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı! ID: " + userId));

        if (user.getRole() == Role.SUPER_ADMIN && !active) {
            throw new RuntimeException("Super admin pasife alınamaz.");
        }

        user.setActive(active);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDTO setUserActiveForAdmin(Long userId, boolean active, User currentUser) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı! ID: " + userId));
        requireCanManageUser(currentUser, target);
        return setUserActive(userId, active);
    }

    @Transactional
    public UserDTO updateJurySpecialties(Long userId, String jurySpecialties, User currentUser) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı! ID: " + userId));
        if (target.getRole() != Role.JURY) {
            throw new RuntimeException("Uzmanlık alanı sadece jüri kullanıcıları için düzenlenebilir.");
        }
        requireCanManageUser(currentUser, target);
        target.setJurySpecialties(jurySpecialties);
        return toUserDto(userRepository.save(target));
    }

    @Transactional
    public UserDTO updateOwnJurySpecialties(User currentUser, String jurySpecialties) {
        if (currentUser == null || currentUser.getRole() != Role.JURY) {
            throw new RuntimeException("Uzmanlık alanlarını düzenlemek için jüri hesabı gerekli.");
        }
        User jury = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Jüri kullanıcısı bulunamadı."));
        jury.setJurySpecialties(jurySpecialties);
        return toUserDto(userRepository.save(jury));
    }

    public void requireCanManageDepartment(User currentUser, Long departmentId) {
        if (currentUser == null) {
            throw new RuntimeException("Kullanıcı bilgisi alınamadı.");
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() != Role.DEPARTMENT_ADMIN || !assignedDepartmentIds(currentUser).contains(departmentId)) {
            throw new RuntimeException("Bu bölüm için işlem yetkiniz yok.");
        }
    }

    private void requireCanManageUser(User currentUser, User target) {
        if (currentUser == null) {
            throw new RuntimeException("Kullanıcı bilgisi alınamadı.");
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() != Role.DEPARTMENT_ADMIN || target.getRole() != Role.JURY) {
            throw new RuntimeException("Bu kullanıcıyı yönetme yetkiniz yok.");
        }
        List<Long> allowedDepartmentIds = assignedDepartmentIds(currentUser);
        boolean intersects = target.getAssignedDepartments().stream()
                .anyMatch(department -> allowedDepartmentIds.contains(department.getId()));
        if (!intersects) {
            throw new RuntimeException("Bu jüri sizin bölümünüze bağlı değil.");
        }
    }

    private List<Long> assignedDepartmentIds(User user) {
        return user.getAssignedDepartments().stream().map(Department::getId).toList();
    }

    private boolean juryMatchesDepartmentScope(User jury, Department department) {
        if (!juryFieldMatchesDepartment(jury, department)) return false;
        String scope = departmentScope(department);
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

    private boolean juryFieldMatchesDepartment(User jury, Department department) {
        String scope = departmentScope(department);
        String expectedField = "SPORT".equals(scope) ? "SPOR" : scope;
        return expectedField.equals(jury.getJuryField());
    }

    private String departmentScope(Department department) {
        String name = normalize(department != null ? department.getName() : "");
        if (containsAny(name, "beden", "antrenor", "spor yoneticiligi", "engelliler", "egzersiz")) return "SPORT";
        if (containsAny(name, "muzik", "muzikoloji")) return "MUSIC";
        if (name.contains("resim")) return "ART";
        if (name.contains("seramik")) return "CERAMIC";
        return "GENERAL";
    }

    private String juryOwnScope(User jury) {
        String text = normalize(String.join(" ",
                jury.getFirstName(),
                jury.getLastName(),
                jury.getJurySpecialties() == null ? "" : jury.getJurySpecialties()));
        if (containsAny(text, "beden", "antrenor", "spor", "parkur", "sprint", "slalom", "engel", "denge")) return "SPORT";
        if (containsAny(text, "muzik", "muzikoloji", "enstruman", "san", "vokal", "ritim", "melodi", "isitme", "kulak", "baglama", "gitar", "piyano", "keman")) return "MUSIC";
        if (containsAny(text, "resim", "cizim", "desen", "gozlem", "kompozisyon", "oran")) return "ART";
        if (containsAny(text, "seramik", "sekillendirme", "modelleme", "hacim", "tasarim")) return "CERAMIC";
        if ("SPOR".equals(jury.getJuryField())) return "SPORT";
        if ("MUSIC".equals(jury.getJuryField())) return "MUSIC";
        if ("ART".equals(jury.getJuryField())) return "ART";
        if ("CERAMIC".equals(jury.getJuryField())) return "CERAMIC";
        return "UNKNOWN";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private String normalize(String value) {
        String lower = value == null ? "" : value.toLowerCase(Locale.forLanguageTag("tr-TR"));
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("ı", "i");
    }
}
