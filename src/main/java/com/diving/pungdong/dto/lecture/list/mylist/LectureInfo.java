package com.diving.pungdong.dto.lecture.list.mylist;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureInfo {
    private Long lectureId;
    private String title;
    private Organization organization;
    private String level;
    private Integer cost;
    private Boolean isRentEquipment;
    private Integer upcomingScheduleCount;
}