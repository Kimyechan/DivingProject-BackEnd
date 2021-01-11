package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LectureJpaRepoTest {
    @Autowired
    private LectureJpaRepo lectureJpaRepo;

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
                .lectureImage(List.of(new LectureImage()))
                .build();

        Lecture savedLecture = lectureJpaRepo.save(lecture);

        assertThat(savedLecture.getId()).isNotNull();
    }
}