package com.diving.pungdong.dto.schedule.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateReq {
    private Long lectureId;
    private Integer period;
    List<ScheduleDetailReq> detailReqList;
}
