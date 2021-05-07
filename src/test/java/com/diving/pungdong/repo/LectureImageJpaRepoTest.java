package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class LectureImageJpaRepoTest {

    @Autowired
    private LectureImageJpaRepo lectureImageJpaRepo;

    @Test
    @DisplayName("강의 이미지 파일 저장")
    public void save() {
        LectureImage lectureImage = LectureImage.builder()
                .fileURI("/lecture/test.png")
                .lecture(new Lecture())
                .build();

        LectureImage savedLectureImage = lectureImageJpaRepo.save(lectureImage);

        assertThat(savedLectureImage.getId()).isNotNull();
        assertThat(savedLectureImage.getFileURI()).isEqualTo(lectureImage.getFileURI());
        assertThat(savedLectureImage.getLecture()).isNotNull();
    }
}