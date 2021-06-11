package com.diving.pungdong.dto.schedule.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScheduleInfo {
    private Long scheduleId;
    private Integer period;
    private LocalTime startTime;
    private Integer currentNumber;
    private Integer maxNumber;
    private List<LocalDate> dates;
}
