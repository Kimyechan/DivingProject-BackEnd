package com.diving.pungdong.dto.review.image.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewImageInfo {
    private Long reviewImageId;
    private String imageUrl;
}
