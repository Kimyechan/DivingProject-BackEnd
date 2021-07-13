package com.diving.pungdong.dto.lecture.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LectureClosedInfo {
    private Boolean isClosed;
}
