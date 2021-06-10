package com.diving.pungdong.dto.schedule.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateInfo {
    @NotNull private Long lectureId;
    @NotNull private Integer period;
    @NotNull private Integer maxNumber;
    @NotNull private LocalTime startTime;
    @NotEmpty private List<LocalDate> dates;
}
