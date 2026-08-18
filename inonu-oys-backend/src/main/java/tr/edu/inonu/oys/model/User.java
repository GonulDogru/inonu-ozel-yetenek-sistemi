package tr.edu.inonu.oys.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String juryField;

    @Column(columnDefinition = "TEXT")
    private String jurySpecialties;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "applicant", fetch = FetchType.LAZY)
    @JsonManagedReference("user-applications")
    private List<Application> applications;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "jury_assignments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    @JsonManagedReference("user-departments")
    private Set<Department> assignedDepartments = new HashSet<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getJuryField() { return juryField; }
    public void setJuryField(String juryField) { this.juryField = juryField; }
    public String getJurySpecialties() { return jurySpecialties; }
    public void setJurySpecialties(String jurySpecialties) { this.jurySpecialties = jurySpecialties; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Application> getApplications() { return applications; }
    public void setApplications(List<Application> applications) { this.applications = applications; }
    public Set<Department> getAssignedDepartments() { return assignedDepartments; }
    public void setAssignedDepartments(Set<Department> assignedDepartments) { this.assignedDepartments = assignedDepartments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
