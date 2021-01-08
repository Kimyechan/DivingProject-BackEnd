package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.LectureJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureJpaRepo lectureJpaRepo;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRepo.save(lecture);
    }

    public Page<Lecture> getListByRegion(String region, @NotEmpty Pageable pageable) {
        return lectureJpaRepo.findByRegion(region, pageable);
    }
}
