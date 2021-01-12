package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.instructor.Instructor;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LectureJpaRepoTest {
    @Autowired
    private LectureJpaRepo lectureJpaRepo;

    @Autowired
    private LectureImageJpaRepo lectureImageJpaReop;

    @Test
    @DisplayName("강의 저장")
    public void save() {
        Lecture lecture = Lecture.builder()
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .groupName("AIDA")
                .certificateKind("Level1")
                .price(100000)
                .period(4)
                .studentCount(5)
                .instructor(new Instructor())
                .swimmingPool(new SwimmingPool())
                .lectureImages(List.of(new LectureImage()))
                .build();

        Lecture savedLecture = lectureJpaRepo.save(lecture);

        assertThat(savedLecture.getId()).isNotNull();
    }

    @Test
    @DisplayName("지역별 강의 조회")
    public void selectByRegion() {
        createLecture();

        String region = "서울";
        Pageable pageable = PageRequest.of(1, 5);
        Page<Lecture> lectures = lectureJpaRepo.findByRegion(region, pageable);

        System.out.println(lectures.getContent().get(0).getLectureImages());
        assertThat(lectures.getSize()).isEqualTo(5);
    }

    @Transactional
    public void createLecture() {
        for (long i = 0; i < 15; i++) {
            Lecture lecture = Lecture.builder()
                    .title("강의" + i)
                    .description("내용" + i)
                    .classKind("스쿠버 다이빙")
                    .groupName("AIDA")
                    .certificateKind("Level1")
                    .price(100000)
                    .period(4)
                    .studentCount(5)
                    .region("서울")
                    .build();

            lectureJpaRepo.save(lecture);

            LectureImage lectureImage = LectureImage.builder()
                    .fileURI("Image URL 주소")
                    .lecture(lecture)
                    .build();
            lecture.getLectureImages().add(lectureImage);

            lectureImageJpaReop.save(lectureImage);
        }
    }
}