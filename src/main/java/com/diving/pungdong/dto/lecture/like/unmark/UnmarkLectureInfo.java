package com.diving.pungdong.dto.lecture.like.unmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnmarkLectureInfo {
    @NotNull
    private Long lectureId;
}
