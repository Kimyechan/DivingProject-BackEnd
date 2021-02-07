package com.diving.pungdong.model.schedule;

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
