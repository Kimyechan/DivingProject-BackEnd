package com.diving.pungdong.dto.lecture.create;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateLectureRes {
    private Long lectureId;
    private String title;
    private String instructorName;
}
