package tr.edu.inonu.oys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.PlacementService;

import java.util.List;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {
    private final PlacementService placementService;
    private final AuditLogService auditLogService;

    public PlacementController(PlacementService placementService, AuditLogService auditLogService) {
        this.placementService = placementService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/departments/{departmentId}/publish")
    public ResponseEntity<?> publish(@PathVariable Long departmentId, @AuthenticationPrincipal User currentUser) {
        try {
            List<ApplicationDTO> result = placementService.calculateAndPublish(departmentId);
            auditLogService.record(currentUser, "PLACEMENT_PUBLISHED", "DEPARTMENT", departmentId,
                    "Bölüm #" + departmentId, result.size() + " aday için yerleştirme sonucu yayımlandı.");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
