package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.repo.LectureJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@Transactional
class LectureServiceTest {
    private LectureService lectureService;

    @Mock
    private LectureJpaRepo lectureJpaRepo;

    @Mock
    private LectureImageService lectureImageService;
    @Mock
    private S3Uploader s3Uploader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lectureService = new LectureService(lectureJpaRepo, lectureImageService, s3Uploader);
    }

    @Test
    @DisplayName("강의 생성")
    public void createLecture() {
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
                .build();

        given(lectureJpaRepo.save(lecture)).willReturn(lecture);

        Lecture savedLecture = lectureService.saveLecture(lecture);

        assertThat(savedLecture.getTitle()).isEqualTo(savedLecture.getTitle());
    }
}