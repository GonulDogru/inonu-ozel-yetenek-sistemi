package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class JuryScoreRequest {
    @NotNull
    private Long applicationId;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double score;
    private String comment;
    private String criteriaScores;

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCriteriaScores() { return criteriaScores; }
    public void setCriteriaScores(String criteriaScores) { this.criteriaScores = criteriaScores; }
}
