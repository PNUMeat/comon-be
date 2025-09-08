package site.codemonster.comon.global.util.dateUtils;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DateUtils {

    public static String getDayOfWeekInKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    public static int setDayOfWeekBitMask(int currentBits, DayOfWeek dayOfWeek, boolean enabled) {
        int dayBit = 1 << (dayOfWeek.getValue() - 1);
        if (enabled) {
            return currentBits | dayBit;
        } else {
            return currentBits & ~dayBit;
        }
    }

    public static int convertDaysToBitMask(Set<DayOfWeek> days) {
        if (days == null) {
            return 0;
        }

        int mask = 0;
        for (DayOfWeek day : days) {
            mask = setDayOfWeekBitMask(mask, day, true);
        }
        return mask;
    }

    public static Set<DayOfWeek> convertBitMaskToDays(int mask) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            int dayBit = 1 << (day.getValue() - 1);
            if ((mask & dayBit) != 0) {
                days.add(day);
            }
        }
        return days;
    }
}
