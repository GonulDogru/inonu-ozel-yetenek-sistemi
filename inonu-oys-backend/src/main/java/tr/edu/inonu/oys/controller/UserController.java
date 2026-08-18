package tr.edu.inonu.oys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.UserDTO;
import tr.edu.inonu.oys.dto.CreateUserRequest;
import tr.edu.inonu.oys.dto.JurySpecialtyRequest;
import tr.edu.inonu.oys.dto.UserActiveRequest;
import tr.edu.inonu.oys.dto.UserDepartmentAssignmentRequest;
import jakarta.validation.Valid;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @Autowired
    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/jury")
    public ResponseEntity<List<UserDTO>> getJuryMembers(@AuthenticationPrincipal User currentUser) {
        List<UserDTO> juryMembers = userService.findJuryMembersForAdmin(currentUser);
        return ResponseEntity.ok(juryMembers);
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAdminUsers(@AuthenticationPrincipal User currentUser) {
        try {
            requireSuperAdmin(currentUser);
            return ResponseEntity.ok(userService.findAllAdminUsers());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser != null && currentUser.getRole() == Role.DEPARTMENT_ADMIN && request.role() != Role.JURY) {
                return ResponseEntity.status(403).body("Bölüm admini sadece jüri hesabı oluşturabilir.");
            }
            User user = new User();
            user.setUsername(request.username());
            user.setPassword(request.password());
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setRole(request.role());
            user.setJuryField(request.juryField());
            User registeredUser = userService.registerUser(user);
            auditLogService.record(currentUser, "USER_CREATED", "USER", registeredUser.getId(),
                    registeredUser.getFirstName() + " " + registeredUser.getLastName(),
                    registeredUser.getRole().name() + " kullanıcısı oluşturuldu.");
            return ResponseEntity.ok(new UserDTO(registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Güvenli silme: kullanıcıyı fiziksel silmek yerine pasife alır.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try {
            UserDTO updated = userService.setUserActiveForAdmin(id, false, currentUser);
            auditLogService.record(currentUser, "USER_DEACTIVATED", "USER", updated.getId(),
                    updated.getFirstName() + " " + updated.getLastName(), "Kullanıcı pasife alındı.");
            return ResponseEntity.ok("Kullanıcı pasife alındı.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> setUserActive(@PathVariable Long id, @Valid @RequestBody UserActiveRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        try {
            UserDTO updated = userService.setUserActiveForAdmin(id, request.active(), currentUser);
            auditLogService.record(currentUser, request.active() ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                    "USER", updated.getId(), updated.getFirstName() + " " + updated.getLastName(),
                    request.active() ? "Kullanıcı aktifleştirildi." : "Kullanıcı pasife alındı.");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/jury-specialties")
    public ResponseEntity<?> updateJurySpecialties(@PathVariable Long id,
                                                   @RequestBody JurySpecialtyRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        try {
            UserDTO updated = userService.updateJurySpecialties(id, request.jurySpecialties(), currentUser);
            auditLogService.record(currentUser, "JURY_SPECIALTIES_UPDATED", "USER", updated.getId(),
                    updated.getFirstName() + " " + updated.getLastName(), "Jüri uzmanlık alanları güncellendi.");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/departments")
    public ResponseEntity<?> setDepartmentAssignment(@PathVariable Long id,
            @Valid @RequestBody UserDepartmentAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            requireSuperAdmin(currentUser);
            UserDTO updated = userService.setDepartmentAssignment(id, request.departmentId(), request.assigned());
            auditLogService.record(currentUser,
                    request.assigned() ? "ADMIN_DEPARTMENT_ASSIGNED" : "ADMIN_DEPARTMENT_REMOVED",
                    "USER", updated.getId(), updated.getFirstName() + " " + updated.getLastName(),
                    "Admin bölüm yetkisi güncellendi. Bölüm #" + request.departmentId());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void requireSuperAdmin(User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new RuntimeException("Bu işlem için super admin yetkisi gereklidir.");
        }
    }
}
