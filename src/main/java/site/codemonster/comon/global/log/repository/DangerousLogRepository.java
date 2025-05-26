package site.codemonster.comon.global.log.repository;

import site.codemonster.comon.global.log.entity.DangerousLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DangerousLogRepository extends JpaRepository<DangerousLog, Long> {
}
