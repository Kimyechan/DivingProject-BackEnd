package com.diving.pungdong.dto.lecture.like.mark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkLectureInfo {
    @NotNull private Long lectureId;
}
