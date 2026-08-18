package tr.edu.inonu.oys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import tr.edu.inonu.oys.dto.DepartmentSettingsRequest;
import tr.edu.inonu.oys.dto.DepartmentDTO;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;

    public DepartmentController(DepartmentService departmentService, AuditLogService auditLogService) {
        this.departmentService = departmentService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.findAllDepartments());
    }

    @PutMapping("/{id}/settings")
    public ResponseEntity<DepartmentDTO> updateSettings(@PathVariable Long id,
            @Valid @RequestBody DepartmentSettingsRequest request,
            @AuthenticationPrincipal User currentUser) {
        DepartmentDTO updated = departmentService.updateSettings(id, request);
        auditLogService.record(currentUser, "DEPARTMENT_SETTINGS_UPDATED", "DEPARTMENT", id,
                updated.getName(), "Bölüm kontenjan/yerleştirme ayarları güncellendi.");
        return ResponseEntity.ok(updated);
    }
}
