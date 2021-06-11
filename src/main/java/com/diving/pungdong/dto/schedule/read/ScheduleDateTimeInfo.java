package com.diving.pungdong.dto.schedule.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class ScheduleDateTimeInfo {
    private Long scheduleDateTimeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
}
