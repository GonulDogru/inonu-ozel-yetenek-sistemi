package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String role;
    private String juryField;
    private String jurySpecialties;
    private boolean active;
    private Set<Long> assignedDepartmentIds;
    private Map<Long, String> assignedDepartmentRoles = new HashMap<>();

    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.role = user.getRole().name();
        this.juryField = user.getJuryField();
        this.jurySpecialties = user.getJurySpecialties();
        this.active = user.isActive();
        this.assignedDepartmentIds = user.getAssignedDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet());
        this.assignedDepartmentIds.forEach(departmentId ->
                this.assignedDepartmentRoles.put(departmentId, "PRIMARY"));
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getJuryField() { return juryField; }
    public String getJurySpecialties() { return jurySpecialties; }
    public boolean isActive() { return active; }
    public Set<Long> getAssignedDepartmentIds() { return assignedDepartmentIds; }
    public Map<Long, String> getAssignedDepartmentRoles() { return assignedDepartmentRoles; }
    public void setAssignedDepartmentRoles(Map<Long, String> assignedDepartmentRoles) {
        this.assignedDepartmentRoles = assignedDepartmentRoles;
    }
}
