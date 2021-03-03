package com.diving.pungdong.dto.lecture.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LectureUpdateRes {
    private Long id;
    private String title;
}
