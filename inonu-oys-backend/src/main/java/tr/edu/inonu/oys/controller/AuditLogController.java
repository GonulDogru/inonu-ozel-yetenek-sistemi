package tr.edu.inonu.oys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.AuditLogDTO;
import tr.edu.inonu.oys.service.AuditLogService;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> find(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditLogService.find(action, targetType, query, limit));
    }
}
