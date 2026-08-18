package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.JuryInactiveSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public class JuryInactiveSlotDTO {
    private final Long id;
    private final Long departmentId;
    private final String departmentName;
    private final Long juryId;
    private final LocalDate inactiveDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String reason;

    public JuryInactiveSlotDTO(JuryInactiveSlot slot) {
        this.id = slot.getId();
        this.departmentId = slot.getDepartment().getId();
        this.departmentName = slot.getDepartment().getName();
        this.juryId = slot.getJury().getId();
        this.inactiveDate = slot.getInactiveDate();
        this.startTime = slot.getStartTime();
        this.endTime = slot.getEndTime();
        this.reason = slot.getReason();
    }

    public Long getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Long getJuryId() { return juryId; }
    public LocalDate getInactiveDate() { return inactiveDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getReason() { return reason; }
}
