package tr.edu.inonu.oys.dto;

// Bir jürinin bir adaya verdiği puanın detayını taşımak için DTO
public class JuryScoreDTO {
    private String juryFullName;
    private Double score;

    public JuryScoreDTO(String firstName, String lastName, Double score) {
        this.juryFullName = firstName + " " + lastName;
        this.score = score;
    }

    // Getters
    public String getJuryFullName() { return juryFullName; }
    public Double getScore() { return score; }
}
