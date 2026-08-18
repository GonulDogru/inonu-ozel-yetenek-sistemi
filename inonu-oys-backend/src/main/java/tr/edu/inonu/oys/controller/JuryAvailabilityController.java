package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.inonu.oys.dto.JuryAvailabilityDTO;
import tr.edu.inonu.oys.dto.JuryAvailabilityRequest;
import tr.edu.inonu.oys.service.JuryAvailabilityService;

import java.util.List;

@RestController
@RequestMapping("/api/jury-availability")
public class JuryAvailabilityController {
    private final JuryAvailabilityService availabilityService;

    public JuryAvailabilityController(JuryAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<JuryAvailabilityDTO>> byDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(availabilityService.findByDepartment(departmentId));
    }

    @GetMapping("/jury/{juryId}")
    public ResponseEntity<List<JuryAvailabilityDTO>> byJury(@PathVariable Long juryId) {
        return ResponseEntity.ok(availabilityService.findByJury(juryId));
    }

    @PostMapping
    public ResponseEntity<JuryAvailabilityDTO> create(@Valid @RequestBody JuryAvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuryAvailabilityDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody JuryAvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.update(id, request));
    }
}
