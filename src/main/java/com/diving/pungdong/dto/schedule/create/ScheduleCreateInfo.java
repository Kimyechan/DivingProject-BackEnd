package com.diving.pungdong.dto.schedule.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateInfo {
    @NotNull
    private Long lectureId;

    @NotEmpty
    private List<ScheduleDateTimeCreateInfo> dateTimeCreateInfos;
}
