package site.codemonster.comon.domain.team.dto.response;

import java.time.DayOfWeek;

public record WeeklyGrassItem(
        DayOfWeek dayOfWeek,
        long count
) {
}
