package tr.edu.inonu.oys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.JuryScoreRepository;
import tr.edu.inonu.oys.repository.UserRepository;
import tr.edu.inonu.oys.service.PlacementService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlacementIntegrationTests {
    @Autowired PlacementService placementService;
    @Autowired DepartmentRepository departmentRepository;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired UserRepository userRepository;
    @Autowired JuryScoreRepository juryScoreRepository;

    @Test
    void publishesFormulaBasedPrincipalReserveAndUnsuccessfulResults() {
        Department department = new Department();
        department.setName("Yerleştirme Test Bölümü");
        department.setQuota(2);
        department.setTrimScores(true);
        department = departmentRepository.save(department);

        User[] juries = new User[3];
        for (int index = 0; index < juries.length; index++) {
            juries[index] = userRepository.save(user("7000000000" + index, Role.JURY));
        }

        double[] rawScores = {95, 85, 75, 65, 55};
        for (int candidateIndex = 0; candidateIndex < rawScores.length; candidateIndex++) {
            User applicant = userRepository.save(user("8000000000" + candidateIndex, Role.APPLICANT));
            Application application = new Application();
            application.setApplicant(applicant);
            application.setDepartment(department);
            application.setStatus(ApplicationStatus.COMPLETED);
            application.setTytScore(300.0);
            application.setObp(400.0);
            application = applicationRepository.save(application);
            for (int juryIndex = 0; juryIndex < juries.length; juryIndex++) {
                JuryScore score = new JuryScore();
                score.setApplication(application);
                score.setJury(juries[juryIndex]);
                score.setScore(rawScores[candidateIndex] + juryIndex - 1);
                juryScoreRepository.save(score);
            }
        }

        List<ApplicationDTO> results = placementService.calculateAndPublish(department.getId());

        assertThat(results).hasSize(5);
        assertThat(results.get(0).getPlacementStatus()).isEqualTo("PRINCIPAL");
        assertThat(results.get(1).getPlacementStatus()).isEqualTo("PRINCIPAL");
        assertThat(results.get(2).getPlacementStatus()).isEqualTo("RESERVE");
        assertThat(results.get(3).getPlacementStatus()).isEqualTo("UNSUCCESSFUL");
        assertThat(results.get(4).getPlacementStatus()).isEqualTo("UNSUCCESSFUL");
        assertThat(results).extracting(ApplicationDTO::getPlacementRank).containsExactly(1, 2, 3, 4, 5);
        assertThat(results.get(0).getFinalPlacementScore())
                .isGreaterThan(results.get(4).getFinalPlacementScore());
        assertThat(results).allMatch(ApplicationDTO::isResultPublished);
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
