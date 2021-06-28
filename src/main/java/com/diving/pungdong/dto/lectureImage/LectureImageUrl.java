package com.diving.pungdong.dto.lectureImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LectureImageUrl {
    private Long lectureImageId;
    private String url;
}
