package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.dto.AutoScheduleRequest;
import tr.edu.inonu.oys.dto.AutoScheduleResultDTO;
import tr.edu.inonu.oys.dto.ExamSessionDTO;
import tr.edu.inonu.oys.dto.ExamSessionRequest;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.AutoSchedulerService;
import tr.edu.inonu.oys.service.ExamSessionService;
import tr.edu.inonu.oys.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/exam-sessions")
public class ExamSessionController {
    private final ExamSessionService examSessionService;
    private final AutoSchedulerService autoSchedulerService;
    private final AuditLogService auditLogService;

    public ExamSessionController(ExamSessionService examSessionService, AutoSchedulerService autoSchedulerService,
                                 AuditLogService auditLogService) {
        this.examSessionService = examSessionService;
        this.autoSchedulerService = autoSchedulerService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<ExamSessionDTO>> all() {
        return ResponseEntity.ok(examSessionService.findAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamSessionDTO>> mySessions(@AuthenticationPrincipal User jury) {
        return ResponseEntity.ok(examSessionService.findByJury(jury));
    }

    @PostMapping
    public ResponseEntity<ExamSessionDTO> create(@Valid @RequestBody ExamSessionRequest request,
                                                 @AuthenticationPrincipal User currentUser) {
        ExamSessionDTO created = examSessionService.create(request);
        auditLogService.record(currentUser, "EXAM_SESSION_CREATED", "EXAM_SESSION", created.getId(),
                created.getDepartmentName(), "Sınav oturumu oluşturuldu.");
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamSessionDTO> update(@PathVariable Long id, @Valid @RequestBody ExamSessionRequest request,
                                                 @AuthenticationPrincipal User currentUser) {
        ExamSessionDTO updated = examSessionService.update(id, request);
        auditLogService.record(currentUser, "EXAM_SESSION_UPDATED", "EXAM_SESSION", id,
                updated.getDepartmentName(), "Sınav oturumu güncellendi.");
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<List<ApplicationDTO>> assign(@PathVariable Long id,
                                                       @AuthenticationPrincipal User currentUser) {
        List<ApplicationDTO> assigned = examSessionService.assignCandidates(id).stream().map(ApplicationDTO::new).toList();
        auditLogService.record(currentUser, "EXAM_CANDIDATES_ASSIGNED", "EXAM_SESSION", id,
                "Oturum #" + id, assigned.size() + " aday sınav oturumuna yerleştirildi.");
        return ResponseEntity.ok(assigned);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        examSessionService.publish(id);
        auditLogService.record(currentUser, "EXAM_SESSION_PUBLISHED", "EXAM_SESSION", id,
                "Oturum #" + id, "Sınav oturumu yayımlandı.");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auto-schedule")
    public ResponseEntity<AutoScheduleResultDTO> autoSchedule(@Valid @RequestBody AutoScheduleRequest request,
                                                              @AuthenticationPrincipal User currentUser) {
        AutoScheduleResultDTO result = autoSchedulerService.autoSchedule(request);
        auditLogService.record(currentUser, "EXAM_AUTO_SCHEDULED", "DEPARTMENT", request.departmentId(),
                result.getDepartmentName(), result.getSessionCount() + " otomatik sınav oturumu oluşturuldu.");
        return ResponseEntity.ok(result);
    }
}
