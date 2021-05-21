package com.diving.pungdong.dto.lecture.popularList;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularLectureInfo {
    private Long id;
    private String title;
    private Organization organization;
    private String level;
    private String region;
    private Integer maxNumber;
    private LocalTime lectureTime;
    private String imageUrl;
    private Boolean isMarked;
    private Integer price;
    private List<String> equipmentNames;
    private Float starAvg;
    private Integer reviewCount;
}
