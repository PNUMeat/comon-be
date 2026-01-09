package site.codemonster.comon.domain.teamRecruit.repository;

import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruitImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRecruitImageRepository extends JpaRepository<TeamRecruitImage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TeamRecruitImage ti WHERE ti.teamRecruit.teamRecruitId = :teamRecruitId")
    void deleteTeamRecruitImagesByTeamRecruitId(@Param("teamRecruitId") Long teamRecruitId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TeamRecruitImage ti " +
            "WHERE ti.teamRecruit.teamRecruitId " +
            "IN :teamRecruitIds")
    void deleteByTeamRecruitIds(@Param("teamRecruitIds") List<Long> teamRecruitIds);
}
