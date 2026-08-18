package tr.edu.inonu.oys.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.AuditLogDTO;
import tr.edu.inonu.oys.model.AuditLog;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.repository.AuditLogRepository;

import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(User actor, String action, String targetType, Long targetId, String targetLabel, String description) {
        AuditLog log = new AuditLog();
        if (actor != null) {
            log.setActorId(actor.getId());
            log.setActorUsername(actor.getUsername());
            log.setActorFullName((actor.getFirstName() + " " + actor.getLastName()).trim());
        }
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetLabel(targetLabel);
        log.setDescription(description);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> find(String action, String targetType, String query, int limit) {
        int safeLimit = Math.max(10, Math.min(limit <= 0 ? 100 : limit, 300));
        Specification<AuditLog> spec = Specification.where(null);

        if (action != null && !action.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("action"), action));
        }
        if (targetType != null && !targetType.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("targetType"), targetType));
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("actorFullName")), pattern),
                    cb.like(cb.lower(root.get("actorUsername")), pattern),
                    cb.like(cb.lower(root.get("targetLabel")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            ));
        }

        return auditLogRepository
                .findAll(spec, PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(AuditLogDTO::new)
                .toList();
    }
}
