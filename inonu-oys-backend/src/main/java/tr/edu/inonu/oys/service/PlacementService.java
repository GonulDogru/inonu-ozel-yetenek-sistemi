package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.model.Application;
import tr.edu.inonu.oys.model.ApplicationStatus;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.model.JuryScore;
import tr.edu.inonu.oys.model.PlacementStatus;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.JuryScoreRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PlacementService {
    private final DepartmentRepository departmentRepository;
    private final ApplicationRepository applicationRepository;
    private final JuryScoreRepository juryScoreRepository;

    public PlacementService(DepartmentRepository departmentRepository, ApplicationRepository applicationRepository,
                            JuryScoreRepository juryScoreRepository) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.juryScoreRepository = juryScoreRepository;
    }

    @Transactional
    public List<ApplicationDTO> calculateAndPublish(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı."));
        if (department.getQuota() <= 0) throw new RuntimeException("Bölüm kontenjanı sıfırdan büyük olmalıdır.");

        List<Application> applications = applicationRepository
                .findByDepartmentIdAndStatus(departmentId, ApplicationStatus.COMPLETED);
        if (applications.isEmpty()) throw new RuntimeException("Yerleştirilecek tamamlanmış aday bulunamadı.");

        List<CandidateScore> scores = new ArrayList<>();
        for (Application application : applications) {
            if (application.getTytScore() == null || application.getObp() == null) {
                throw new RuntimeException("Tüm adayların TYT ve OBP bilgisi bulunmalıdır.");
            }
            List<Double> juryScores = juryScoreRepository.findByApplicationId(application.getId())
                    .stream().map(JuryScore::getScore).sorted().toList();
            if (juryScores.size() < 3) throw new RuntimeException("Her aday için en az üç jüri puanı gereklidir.");
            double rawScore = average(juryScores, department.isTrimScores());
            scores.add(new CandidateScore(application, rawScore));
        }

        double cohortMean = scores.stream().mapToDouble(CandidateScore::rawScore).average().orElse(0);
        double standardDeviation = Math.sqrt(scores.stream()
                .mapToDouble(score -> Math.pow(score.rawScore() - cohortMean, 2)).average().orElse(0));

        for (CandidateScore candidate : scores) {
            double standardized = standardDeviation == 0
                    ? 50.0
                    : ((candidate.rawScore() - cohortMean) / standardDeviation) * 10.0 + 50.0;
            double placementScore = standardized * 0.55
                    + candidate.application().getTytScore() * 0.35
                    + candidate.application().getObp() * 0.10;
            Application application = candidate.application();
            application.setAverageScore(round(candidate.rawScore()));
            application.setOyspScore(round(candidate.rawScore()));
            application.setStandardizedOyspScore(round(standardized));
            application.setFinalPlacementScore(round(placementScore));
        }

        scores.sort(Comparator
                .comparing((CandidateScore score) -> score.application().getFinalPlacementScore()).reversed()
                .thenComparing(score -> score.application().getId()));

        int reserveQuota = (int) Math.ceil(department.getQuota() * 0.5);
        LocalDateTime publishedAt = LocalDateTime.now();
        for (int index = 0; index < scores.size(); index++) {
            Application application = scores.get(index).application();
            application.setPlacementRank(index + 1);
            if (index < department.getQuota()) {
                application.setPlacementStatus(PlacementStatus.PRINCIPAL);
            } else if (index < department.getQuota() + reserveQuota) {
                application.setPlacementStatus(PlacementStatus.RESERVE);
            } else {
                application.setPlacementStatus(PlacementStatus.UNSUCCESSFUL);
            }
            application.setResultPublishedAt(publishedAt);
        }
        applicationRepository.saveAll(applications);
        return scores.stream().map(score -> new ApplicationDTO(score.application())).toList();
    }

    private double average(List<Double> values, boolean trim) {
        int start = trim && values.size() >= 3 ? 1 : 0;
        int end = trim && values.size() >= 3 ? values.size() - 1 : values.size();
        return values.subList(start, end).stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record CandidateScore(Application application, double rawScore) {}
}
