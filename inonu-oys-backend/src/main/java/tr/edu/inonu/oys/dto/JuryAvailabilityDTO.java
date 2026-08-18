package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.JuryAvailability;

import java.time.LocalDate;
import java.time.LocalTime;

public class JuryAvailabilityDTO {
    private final Long id;
    private final Long departmentId;
    private final String departmentName;
    private final Long juryId;
    private final String juryName;
    private final LocalDate availableDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public JuryAvailabilityDTO(JuryAvailability availability) {
        this.id = availability.getId();
        this.departmentId = availability.getDepartment().getId();
        this.departmentName = availability.getDepartment().getName();
        this.juryId = availability.getJury().getId();
        this.juryName = availability.getJury().getFirstName() + " " + availability.getJury().getLastName();
        this.availableDate = availability.getAvailableDate();
        this.startTime = availability.getStartTime();
        this.endTime = availability.getEndTime();
    }

    public Long getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getJuryId() { return juryId; }
    public String getJuryName() { return juryName; }
    public LocalDate getAvailableDate() { return availableDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
