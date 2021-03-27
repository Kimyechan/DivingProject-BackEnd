package com.diving.pungdong.dto.lecture.mylist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureInfo {
    private String title;
    private String groupName;
    private String certificateKind;
    private Integer cost;
    private Boolean isRentEquipment;
    private Integer upcomingScheduleCount;
}