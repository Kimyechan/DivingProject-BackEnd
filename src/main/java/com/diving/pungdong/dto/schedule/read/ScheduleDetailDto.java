package com.diving.pungdong.dto.schedule.read;

import com.diving.pungdong.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScheduleDetailDto {
    private Long scheduleDetailId;
    private LocalDate date;
    private List<ScheduleTimeDto> scheduleTimeDtoList;
    private LocalTime lectureTime;
    private Location location;
}
