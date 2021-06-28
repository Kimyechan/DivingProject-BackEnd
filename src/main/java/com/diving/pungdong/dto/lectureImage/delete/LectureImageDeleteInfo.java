package com.diving.pungdong.dto.lectureImage.delete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureImageDeleteInfo {
    private Long lectureId;
    private List<Long> lectureImageIds;
}
