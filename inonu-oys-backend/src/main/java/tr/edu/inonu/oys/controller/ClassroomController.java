package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.ClassroomDTO;
import tr.edu.inonu.oys.dto.ClassroomRequest;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.ClassroomService;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {
    private final ClassroomService classroomService;
    private final AuditLogService auditLogService;

    public ClassroomController(ClassroomService classroomService, AuditLogService auditLogService) {
        this.classroomService = classroomService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ClassroomDTO>> byDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(classroomService.findByDepartment(departmentId));
    }

    @PostMapping
    public ResponseEntity<ClassroomDTO> create(@Valid @RequestBody ClassroomRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        ClassroomDTO created = classroomService.create(request);
        auditLogService.record(currentUser, "CLASSROOM_CREATED", "CLASSROOM", created.getId(),
                created.getName(), "Salon oluşturuldu.");
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomDTO> update(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        ClassroomDTO updated = classroomService.update(id, request);
        auditLogService.record(currentUser, updated.isActive() ? "CLASSROOM_ACTIVATED" : "CLASSROOM_DEACTIVATED",
                "CLASSROOM", updated.getId(), updated.getName(),
                updated.isActive() ? "Salon aktif hale getirildi veya güncellendi." : "Salon pasife alındı veya güncellendi.");
        return ResponseEntity.ok(updated);
    }
}
