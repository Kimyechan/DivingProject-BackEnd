package com.diving.pungdong.dto.lecture.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureImageUpdate {
    String lectureImageURL;
    Boolean isDeleted;
}