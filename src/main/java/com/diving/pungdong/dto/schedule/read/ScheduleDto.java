package com.diving.pungdong.dto.schedule.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScheduleDto {
    private Long scheduleId;
    private Integer period;
    private Integer maxNumber;
    private List<ScheduleDetailDto> scheduleDetails;
}
