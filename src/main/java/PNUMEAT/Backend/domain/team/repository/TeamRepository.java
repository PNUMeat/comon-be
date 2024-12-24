package PNUMEAT.Backend.domain.team.repository;

import PNUMEAT.Backend.domain.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findAll(Pageable pageable);
    Page<Team> findByTeamNameContaining(String keyword, Pageable pageable);
}
