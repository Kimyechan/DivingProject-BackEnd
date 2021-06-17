package com.diving.pungdong.dto.review.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateInfo {
    @NotNull
    private Long reservationId;

    @NotNull
    private Float instructorStar;

    @NotNull
    private Float lectureStar;

    @NotNull
    private Float locationStar;

    @NotEmpty
    private String description;
}