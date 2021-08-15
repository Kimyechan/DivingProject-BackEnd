package com.diving.pungdong.dto.lecture;

import com.diving.pungdong.domain.lecture.Lecture;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
public class LectureTitleUI extends RepresentationModel<LectureTitleUI> {
    private final Long id;
    private final String title;

    public LectureTitleUI(Lecture lecture) {
        this.id = lecture.getId();
        this.title = lecture.getTitle();
    }
}
