package com.diving.pungdong.model.schedule;

import com.diving.pungdong.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailReq {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Location location;
}
