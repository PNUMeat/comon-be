package site.codemonster.comon.domain.team.dto.response;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record TeamDashboardResponse(
        String teamName,
        int nDays,
        LocalDate joinedAt,
        String imageUrl,
        String memberName,
        String description,
        int weeklySolvedDays,
        long consecutiveSolveCount,
        long cumulativeSolveCount,
        List<WeeklyGrassItem> weeklyGrass
) {
    public static TeamDashboardResponse of(
            TeamMember teamMember,
            LocalDate joinedAt,
            Set<DayOfWeek> recommendationDays,
            Map<LocalDate, Long> solveCountsByDate,
            LocalDate today,
            LocalDate lookbackFloor,
            long cumulativeSolveCount
    ) {
        Member member = teamMember.getMember();
        Team team = teamMember.getTeam();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return new TeamDashboardResponse(
                team.getTeamName(),
                recommendationDays.size(),
                joinedAt,
                member.getImageUrl(),
                member.getMemberName(),
                member.getDescription(),
                countSolvedDays(solveCountsByDate, weekStart, today),
                calculateStreak(solveCountsByDate, recommendationDays, today, lookbackFloor),
                cumulativeSolveCount,
                buildWeeklyGrass(solveCountsByDate, weekStart, today)
        );
    }

    private static int countSolvedDays(Map<LocalDate, Long> solveCountsByDate, LocalDate weekStart, LocalDate today) {
        int days = 0;
        for (LocalDate date = weekStart; !date.isAfter(today); date = date.plusDays(1)) {
            if (solveCountsByDate.getOrDefault(date, 0L) > 0L) {
                days++;
            }
        }
        return days;
    }

    private static long calculateStreak(Map<LocalDate, Long> solveCountsByDate, Set<DayOfWeek> recommendationDays, LocalDate today, LocalDate lookbackFloor) {
        LocalDate streakStart = lookbackFloor;
        for (LocalDate date = today; !date.isBefore(lookbackFloor); date = date.minusDays(1)) {
            boolean missedRecommendedDay = date.isBefore(today)
                    && recommendationDays.contains(date.getDayOfWeek())
                    && solveCountsByDate.getOrDefault(date, 0L) == 0L;
            if (missedRecommendedDay) {
                streakStart = date.plusDays(1);
                break;
            }
        }

        long total = 0L;
        for (LocalDate date = streakStart; !date.isAfter(today); date = date.plusDays(1)) {
            total += solveCountsByDate.getOrDefault(date, 0L);
        }
        return total;
    }

    private static List<WeeklyGrassItem> buildWeeklyGrass(Map<LocalDate, Long> solveCountsByDate, LocalDate weekStart, LocalDate today) {
        List<WeeklyGrassItem> grass = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            long count = date.isAfter(today) ? 0L : solveCountsByDate.getOrDefault(date, 0L);
            grass.add(new WeeklyGrassItem(date.getDayOfWeek(), count));
        }
        return grass;
    }
}
