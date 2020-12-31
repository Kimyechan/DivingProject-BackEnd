package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.LectureJpaRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureJpaRep lectureJpaRep;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRep.save(lecture);
    }
}
