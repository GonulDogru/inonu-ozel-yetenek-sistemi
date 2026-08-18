package tr.edu.inonu.oys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.SystemSettingsDTO;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.SystemSettingsService;

@RestController
@RequestMapping("/api/system-settings")
public class SystemSettingsController {
    private final SystemSettingsService settingsService;
    private final AuditLogService auditLogService;

    public SystemSettingsController(SystemSettingsService settingsService, AuditLogService auditLogService) {
        this.settingsService = settingsService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<SystemSettingsDTO> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SystemSettingsDTO request, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Bu işlem için super admin yetkisi gereklidir.");
        }
        SystemSettingsDTO updated = settingsService.update(request);
        auditLogService.record(currentUser, "SYSTEM_SETTINGS_UPDATED", "SYSTEM_SETTINGS", 1L,
                "Sistem Ayarları", "Başvuru ve belge kuralları güncellendi.");
        return ResponseEntity.ok(updated);
    }
}
