package com.diving.pungdong.dto.lecture.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScheduleDto {
    private Integer period;
    private List<ScheduleDetailDto> scheduleDetails;
}
