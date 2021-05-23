package com.diving.pungdong.dto.lectureImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LectureImageInfo {
    private Long lectureId;
    private List<String> imageUris;
}
