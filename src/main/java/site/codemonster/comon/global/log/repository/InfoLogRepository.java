package site.codemonster.comon.global.log.repository;

import site.codemonster.comon.global.log.entity.InfoLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoLogRepository extends JpaRepository<InfoLog, Long> {
}
