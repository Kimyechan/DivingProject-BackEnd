package com.diving.pungdong.dto.lecture.list.mylist;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyLectureInfo {
    private Long id;
    private String title;
    private Organization organization;
    private String level;
    private String region;
    private Integer maxNumber;
    private Integer period;
    private LocalTime lectureTime;
    private String imageUrl;
    private Integer price;
    private List<String> equipmentNames;
    private Long leftScheduleDate;
    private Boolean isClosed;
}