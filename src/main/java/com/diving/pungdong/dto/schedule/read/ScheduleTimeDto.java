package com.diving.pungdong.dto.schedule.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class ScheduleTimeDto {
    private Long scheduleTimeId;
    private LocalTime startTime;
    private Integer currentNumber;
}
