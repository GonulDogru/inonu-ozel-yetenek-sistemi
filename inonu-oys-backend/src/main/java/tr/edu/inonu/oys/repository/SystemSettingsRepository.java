package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.inonu.oys.model.SystemSettings;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
}
