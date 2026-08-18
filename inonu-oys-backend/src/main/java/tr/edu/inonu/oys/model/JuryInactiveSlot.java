package tr.edu.inonu.oys.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "jury_inactive_slots")
public class JuryInactiveSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id", nullable = false)
    private User jury;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private LocalDate inactiveDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getJury() { return jury; }
    public void setJury(User jury) { this.jury = jury; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public LocalDate getInactiveDate() { return inactiveDate; }
    public void setInactiveDate(LocalDate inactiveDate) { this.inactiveDate = inactiveDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
