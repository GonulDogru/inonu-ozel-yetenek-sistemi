package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.ExamSessionJury;

public class ExamSessionJuryDTO {
    private final Long juryId;
    private final String juryName;
    private final String assignmentRole;
    private final boolean replacement;

    public ExamSessionJuryDTO(ExamSessionJury assignment) {
        this.juryId = assignment.getJury().getId();
        this.juryName = assignment.getJury().getFirstName() + " " + assignment.getJury().getLastName();
        this.assignmentRole = assignment.getAssignmentRole().name();
        this.replacement = assignment.isReplacement();
    }

    public Long getJuryId() { return juryId; }
    public String getJuryName() { return juryName; }
    public String getAssignmentRole() { return assignmentRole; }
    public boolean isReplacement() { return replacement; }
}
