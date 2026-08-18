package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.AuditLog;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private final Long id;
    private final LocalDateTime createdAt;
    private final Long actorId;
    private final String actorUsername;
    private final String actorFullName;
    private final String action;
    private final String targetType;
    private final Long targetId;
    private final String targetLabel;
    private final String description;

    public AuditLogDTO(AuditLog log) {
        this.id = log.getId();
        this.createdAt = log.getCreatedAt();
        this.actorId = log.getActorId();
        this.actorUsername = log.getActorUsername();
        this.actorFullName = log.getActorFullName();
        this.action = log.getAction();
        this.targetType = log.getTargetType();
        this.targetId = log.getTargetId();
        this.targetLabel = log.getTargetLabel();
        this.description = log.getDescription();
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getActorFullName() { return actorFullName; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getTargetLabel() { return targetLabel; }
    public String getDescription() { return description; }
}
