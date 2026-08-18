package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.dto.JuryAssignmentRequest;
import tr.edu.inonu.oys.dto.JuryInactiveSlotDTO;
import tr.edu.inonu.oys.dto.JuryInactiveSlotRequest;
import tr.edu.inonu.oys.dto.JuryScoreRequest;
import tr.edu.inonu.oys.dto.JurySpecialtyRequest;
import tr.edu.inonu.oys.dto.UserDTO;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.JuryService;
import tr.edu.inonu.oys.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/jury")
public class JuryController {
    private final JuryService juryService;
    private final AuditLogService auditLogService;
    private final UserService userService;

    public JuryController(JuryService juryService, AuditLogService auditLogService, UserService userService) {
        this.juryService = juryService;
        this.auditLogService = auditLogService;
        this.userService = userService;
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationDTO>> getAssignedApplications(@AuthenticationPrincipal User jury) {
        return ResponseEntity.ok(juryService.getAssignedApplications(jury));
    }

    @GetMapping("/inactive-slots")
    public ResponseEntity<List<JuryInactiveSlotDTO>> inactiveSlots(@AuthenticationPrincipal User jury) {
        return ResponseEntity.ok(juryService.getInactiveSlots(jury));
    }

    @PatchMapping("/specialties")
    public ResponseEntity<?> updateOwnSpecialties(@RequestBody JurySpecialtyRequest request,
                                                  @AuthenticationPrincipal User jury) {
        try {
            UserDTO updated = userService.updateOwnJurySpecialties(jury, request.jurySpecialties());
            auditLogService.record(jury, "JURY_SPECIALTIES_UPDATED", "JURY", jury.getId(),
                    jury.getFirstName() + " " + jury.getLastName(), "Jüri kendi uzmanlık alanlarını güncelledi.");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/inactive-slots")
    public ResponseEntity<JuryInactiveSlotDTO> createInactiveSlot(@Valid @RequestBody JuryInactiveSlotRequest request,
                                                                 @AuthenticationPrincipal User jury) {
        JuryInactiveSlotDTO created = juryService.createInactiveSlot(request, jury);
        auditLogService.record(jury, "JURY_INACTIVE_SLOT_CREATED", "JURY", jury.getId(),
                jury.getFirstName() + " " + jury.getLastName(), "Jüri müsait olmadığı zaman aralığı ekledi.");
        return ResponseEntity.ok(created);
    }

    @PostMapping("/score")
    public ResponseEntity<?> submitScore(@Valid @RequestBody JuryScoreRequest request,
                                         @AuthenticationPrincipal User jury) {
        try {
            juryService.saveScore(request, jury);
            return ResponseEntity.ok("Puan başarıyla kaydedildi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assign(@Valid @RequestBody JuryAssignmentRequest request,
                                    @AuthenticationPrincipal User currentUser) {
        userService.requireCanManageDepartment(currentUser, request.departmentId());
        juryService.assignDepartment(request.juryId(), request.departmentId(), request.assignmentRole());
        auditLogService.record(currentUser, "JURY_DEPARTMENT_ASSIGNED", "JURY", request.juryId(),
                "Jüri #" + request.juryId(), "Bölüm #" + request.departmentId() + " ataması yapıldı.");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(@Valid @RequestBody JuryAssignmentRequest request,
                                    @AuthenticationPrincipal User currentUser) {
        userService.requireCanManageDepartment(currentUser, request.departmentId());
        juryService.removeDepartment(request.juryId(), request.departmentId());
        auditLogService.record(currentUser, "JURY_DEPARTMENT_REMOVED", "JURY", request.juryId(),
                "Jüri #" + request.juryId(), "Bölüm #" + request.departmentId() + " ataması kaldırıldı.");
        return ResponseEntity.ok().build();
    }
}
