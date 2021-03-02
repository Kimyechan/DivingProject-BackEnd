package com.diving.pungdong.dto.lecture.detail;

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
    private LocalDate date;
    private List<LocalTime> startTimes;
    private LocalTime lectureTime;
    private Location location;
}
