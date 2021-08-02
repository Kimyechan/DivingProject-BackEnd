package com.diving.pungdong.dto.lectureMark;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
public class LectureMarkModel extends RepresentationModel<LectureMarkModel> {
    private boolean isMarked;

    public LectureMarkModel(boolean isMarked) {
        this.isMarked = isMarked;
    }
}
