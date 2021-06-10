package com.diving.pungdong.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ReviewInfo {
    private Long id;
    private Float instructorStar;
    private Float lectureStar;
    private Float locationStar;
    private Float totalStarAvg;
    private String description;
    private LocalDate writeDate;
    private List<String> reviewImageUrls;
}
