package com.diving.pungdong.dto.lecture.detail;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LectureDetail {
    private Long id;
    private String title;
    private String classKind;
    private Organization organization;
    private String level;
    private Integer period;
    private String description;
    private Integer price;
    private String region;
    private Float reviewTotalAvg;
    private Integer reviewCount;
    private Boolean isMarked;
}
