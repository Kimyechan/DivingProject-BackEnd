package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.repo.LectureImageJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@Transactional
class LectureImageServiceTest {
    private LectureImageService lectureImageService;

    @Mock
    private LectureImageJpaRepo lectureImageJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lectureImageService = new LectureImageService(lectureImageJpaRepo);
   }

    @Test
    @DisplayName("강의 사진 저장")
    public void saveLectureImage() {
        LectureImage lectureImage = LectureImage.builder()
                .fileName("abc.png")
                .lecture(any())
                .build();

        given(lectureImageService.saveLectureImage(lectureImage)).willReturn(lectureImage);
        LectureImage savedLectureImage = lectureImageService.saveLectureImage(lectureImage);

        assertThat(savedLectureImage.getFileName()).isEqualTo(lectureImage.getFileName());
    }
}