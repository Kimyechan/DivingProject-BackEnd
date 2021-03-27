package com.diving.pungdong.dto.schedule.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleTimeInfo {
    private Long lectureId;
    private Long scheduleTimeId;
}
