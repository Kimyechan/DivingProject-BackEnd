package com.diving.pungdong.dto.schedule.create;

import com.diving.pungdong.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailReq {
    private LocalDate date;
    private List<LocalTime> startTimes;
    private LocalTime lectureTime;
    private Location location;
}
