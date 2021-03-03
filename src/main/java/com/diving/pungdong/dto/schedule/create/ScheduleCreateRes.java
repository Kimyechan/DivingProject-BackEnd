package com.diving.pungdong.dto.schedule.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRes {
    private Long lectureId;
    private Long scheduleId;
}
