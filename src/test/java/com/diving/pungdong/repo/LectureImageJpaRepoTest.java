package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class LectureImageJpaRepoTest {

    @Autowired
    private LectureImageJpaRepo lectureImageJpaRepo;

    @Autowired
    private TestEntityManager em;

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

    public Long saveLectureImagesWithLecture() {
        Lecture lecture = Lecture.builder().build();
        Lecture savedLecture = em.persist(lecture);

        for (int i = 0; i < 3; i++) {
            LectureImage lectureImage = LectureImage.builder()
                    .fileURI("강의 이미지 URL" + i)
                    .lecture(savedLecture)
                    .build();
            em.persist(lectureImage);
        }
        em.flush();
        em.clear();

        return savedLecture.getId();
    }

    @Test
    @DisplayName("해당 강의의 강의 이미지 목록 조회")
    public void findLectureImages() {
        Long lectureId = saveLectureImagesWithLecture();

        List<LectureImage> lectureImages = lectureImageJpaRepo.findAllByLectureId(lectureId);

        assertThat(lectureImages).isNotEmpty();
    }
}