package com.diving.pungdong.dto.lecture.list;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureInfo {
    private Long id;
    private String title;
    private Organization organization;
    private String level;
    private String region;
    private Integer period;
    private LocalTime lectureTime;
    private String imageUrl;
    private Boolean isMarked;
    private Integer price;
    private List<String> equipmentNames;
    private Float starAvg;
    private Integer reviewCount;
}
