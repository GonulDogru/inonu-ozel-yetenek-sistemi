package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.Classroom;

public class ClassroomDTO {
    private final Long id;
    private final Long departmentId;
    private final String departmentName;
    private final String name;
    private final int capacity;
    private final String building;
    private final boolean active;

    public ClassroomDTO(Classroom classroom) {
        this.id = classroom.getId();
        this.departmentId = classroom.getDepartment().getId();
        this.departmentName = classroom.getDepartment().getName();
        this.name = classroom.getName();
        this.capacity = classroom.getCapacity();
        this.building = classroom.getBuilding();
        this.active = classroom.isActive();
    }

    public Long getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getBuilding() { return building; }
    public boolean isActive() { return active; }
}
