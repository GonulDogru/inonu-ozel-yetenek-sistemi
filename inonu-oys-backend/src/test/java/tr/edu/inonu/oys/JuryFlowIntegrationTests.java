package tr.edu.inonu.oys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.JuryScoreRequest;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.UserRepository;
import tr.edu.inonu.oys.service.JuryService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JuryFlowIntegrationTests {
    @Autowired JuryService juryService;
    @Autowired UserRepository userRepository;
    @Autowired DepartmentRepository departmentRepository;
    @Autowired ApplicationRepository applicationRepository;

    @Test
    void thirdAssignedJuryScoreCompletesApplicationAndCalculatesAverage() {
        Department department = new Department();
        department.setName("Test Bölümü");
        department = departmentRepository.save(department);

        User applicant = user("20000000000", Role.APPLICANT);
        applicant = userRepository.save(applicant);

        Application application = new Application();
        application.setApplicant(applicant);
        application.setDepartment(department);
        application.setStatus(ApplicationStatus.PENDING_EVALUATION);
        application = applicationRepository.save(application);

        double[] values = {70, 80, 90};
        for (int i = 0; i < values.length; i++) {
            User jury = user("3000000000" + i, Role.JURY);
            jury.getAssignedDepartments().add(department);
            jury = userRepository.save(jury);
            JuryScoreRequest request = new JuryScoreRequest();
            request.setApplicationId(application.getId());
            request.setScore(values[i]);
            juryService.saveScore(request, jury);
        }

        Application completed = applicationRepository.findById(application.getId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(ApplicationStatus.COMPLETED);
        assertThat(completed.getOyspScore()).isEqualTo(80);
        assertThat(completed.getAverageScore()).isEqualTo(80);
    }

    private User user(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded");
        user.setFirstName("Test");
        user.setLastName(role.name());
        user.setRole(role);
        return user;
    }
}
