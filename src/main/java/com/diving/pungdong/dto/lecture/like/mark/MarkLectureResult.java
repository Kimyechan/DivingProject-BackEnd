package com.diving.pungdong.dto.lecture.like.mark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MarkLectureResult {
    private Long lectureMarkId;
    private Long lectureId;
    private Long accountId;
}
