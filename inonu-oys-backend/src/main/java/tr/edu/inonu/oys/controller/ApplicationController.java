package tr.edu.inonu.oys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.dto.ApplicationAcademicRequest;
import jakarta.validation.Valid;
import tr.edu.inonu.oys.dto.JuryScoreDTO;
import tr.edu.inonu.oys.model.Application;
import tr.edu.inonu.oys.repository.JuryScoreRepository;
import tr.edu.inonu.oys.service.ApplicationService;
import tr.edu.inonu.oys.service.AuditLogService;
import tr.edu.inonu.oys.service.PdfGeneratorService;
import tr.edu.inonu.oys.service.SystemSettingsService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JuryScoreRepository juryScoreRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final AuditLogService auditLogService;
    private final SystemSettingsService systemSettingsService;

    @Autowired
    public ApplicationController(ApplicationService applicationService, JuryScoreRepository juryScoreRepository,
                                 PdfGeneratorService pdfGeneratorService, AuditLogService auditLogService,
                                 SystemSettingsService systemSettingsService) {
        this.applicationService = applicationService;
        this.juryScoreRepository = juryScoreRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.auditLogService = auditLogService;
        this.systemSettingsService = systemSettingsService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> createApplication(
            @RequestParam("tytScore") Double tytScore,
            @RequestParam(value = "obp", required = false) Double obp,
            @RequestParam("faculty") String faculty,
            @RequestParam("departmentId") Long departmentId,
            @RequestParam(value = "performancePreferences", required = false) String performancePreferences,
            @RequestParam("isNational") Boolean isNational,
            @RequestParam("isDisabled") Boolean isDisabled,
            @RequestParam(value = "osymDoc", required = false) MultipartFile osymDoc,
            @RequestParam(value = "diplomaDoc", required = false) MultipartFile diplomaDoc,
            @RequestParam(value = "healthDoc", required = false) MultipartFile healthDoc,
            @RequestParam(value = "photoDoc", required = false) MultipartFile photoDoc,
            @RequestParam(value = "nationalDoc", required = false) MultipartFile nationalDoc,
            @RequestParam(value = "disabledDoc", required = false) MultipartFile disabledDoc,
            @AuthenticationPrincipal tr.edu.inonu.oys.model.User currentUser) {
        try {
            applicationService.createApplication(currentUser.getUsername(), tytScore, obp, faculty, departmentId, performancePreferences, isNational, isDisabled,
                    osymDoc, diplomaDoc, healthDoc, photoDoc, nationalDoc, disabledDoc);
            return ResponseEntity.ok("Başvuru başarıyla alındı.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<ApplicationDTO> getApplicationByUsername(@PathVariable String username,
            @AuthenticationPrincipal tr.edu.inonu.oys.model.User currentUser) {
        if (!currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        Optional<ApplicationDTO> application = applicationService.getApplicationByUsername(username);
        return application.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApplicationDTO>> getAllApplications(
            @AuthenticationPrincipal tr.edu.inonu.oys.model.User currentUser) {
        List<ApplicationDTO> applications = applicationService.getAllApplicationsForAdmin(currentUser);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                          @AuthenticationPrincipal tr.edu.inonu.oys.model.User currentUser) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body("Yeni durum (status) bilgisi g?nderilmedi.");
            }
            applicationService.updateApplicationStatus(id, newStatus);
            Application application = applicationService.findById(id);
            String targetLabel = application.getApplicant() != null
                    ? application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName()
                    : "Ba?vuru #" + id;
            auditLogService.record(currentUser, "APPLICATION_STATUS_CHANGED", "APPLICATION", id,
                    targetLabel, "Ba?vuru durumu " + newStatus + " olarak g?ncellendi.");
            return ResponseEntity.ok("Ba?vuru durumu ba?ar?yla g?ncellendi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/academic")
    public ResponseEntity<ApplicationDTO> updateAcademic(@PathVariable Long id,
            @Valid @RequestBody ApplicationAcademicRequest request) {
        return ResponseEntity.ok(applicationService.updateObp(id, request.obp()));
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<List<JuryScoreDTO>> getScoresForApplication(@PathVariable Long id) {
        List<JuryScoreDTO> scores = juryScoreRepository.findScoresByApplicationIdAsDTO(id);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/{id}/exam-document")
    public ResponseEntity<InputStreamResource> downloadExamDocument(@PathVariable Long id) {
        if (!systemSettingsService.getOrCreate().isExamDocumentEnabled()) {
            return ResponseEntity.status(403).build();
        }
        Application application = applicationService.findById(id);
        ByteArrayInputStream pdfStream = pdfGeneratorService.generateExamDocument(application);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=sinav-giris-belgesi-" + id + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

    @GetMapping("/{id}/result-document")
    public ResponseEntity<InputStreamResource> downloadResultDocument(@PathVariable Long id) {
        if (!systemSettingsService.getOrCreate().isResultDocumentEnabled()) {
            return ResponseEntity.status(403).build();
        }
        Application application = applicationService.findById(id);
        if (application.getResultPublishedAt() == null || application.getPlacementStatus() == null) {
            return ResponseEntity.status(403).build();
        }
        ByteArrayInputStream pdfStream = pdfGeneratorService.generateResultDocument(application);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=sinav-sonuc-belgesi-" + id + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
