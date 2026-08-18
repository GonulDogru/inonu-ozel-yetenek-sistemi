package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.ApproveCandidateJuryAssignmentsRequest;
import tr.edu.inonu.oys.dto.CandidateJuryAssignmentDTO;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.CandidateJuryAssignmentService;

import java.util.List;

@RestController
@RequestMapping("/api/candidate-jury-assignments")
public class CandidateJuryAssignmentController {
    private final CandidateJuryAssignmentService assignmentService;
    private final AuditLogService auditLogService;

    public CandidateJuryAssignmentController(CandidateJuryAssignmentService assignmentService,
                                             AuditLogService auditLogService) {
        this.assignmentService = assignmentService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<CandidateJuryAssignmentDTO>> list(@PathVariable Long applicationId,
                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(assignmentService.listForApplication(applicationId, currentUser));
    }

    @PostMapping("/application/{applicationId}/suggest")
    public ResponseEntity<?> suggest(@PathVariable Long applicationId,
                                     @AuthenticationPrincipal User currentUser) {
        try {
            List<CandidateJuryAssignmentDTO> suggestions = assignmentService.suggest(applicationId, currentUser);
            auditLogService.record(currentUser, "CANDIDATE_JURY_SUGGESTED", "APPLICATION", applicationId,
                    "Başvuru #" + applicationId, "Aday için otomatik jüri önerileri üretildi.");
            return ResponseEntity.ok(suggestions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approve(@Valid @RequestBody ApproveCandidateJuryAssignmentsRequest request,
                                     @AuthenticationPrincipal User currentUser) {
        try {
            List<CandidateJuryAssignmentDTO> approved = assignmentService.approve(
                    request.applicationId(), request.juryIds(), currentUser);
            auditLogService.record(currentUser, "CANDIDATE_JURY_APPROVED", "APPLICATION", request.applicationId(),
                    "Başvuru #" + request.applicationId(), "Aday-jüri eşleşmeleri onaylandı.");
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
