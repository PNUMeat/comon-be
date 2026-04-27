package site.codemonster.comon.domain.alarm.dto;

import site.codemonster.comon.domain.alarm.entity.Alarm;

public record AlarmResponse(
        Long alarmId,
        String title,
        String content
) {

    public AlarmResponse(Alarm alarm) {
        this(
                alarm.getId(),
                alarm.getTitle(),
                alarm.getContent()
        );
    }
}
