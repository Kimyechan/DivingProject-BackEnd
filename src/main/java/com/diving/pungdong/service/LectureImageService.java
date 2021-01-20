package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.repo.LectureImageJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureImageService {
    private final LectureImageJpaRepo lectureImageJpaRepo;

    public LectureImage saveLectureImage(LectureImage lectureImage) {
        return lectureImageJpaRepo.save(lectureImage);
    }

    public void deleteByURL(String lectureImageURL) {
        lectureImageJpaRepo.deleteByFileURI(lectureImageURL);
    }
}
