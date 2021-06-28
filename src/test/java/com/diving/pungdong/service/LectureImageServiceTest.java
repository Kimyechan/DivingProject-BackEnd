package com.diving.pungdong.service;

import com.diving.pungdong.repo.LectureImageJpaRepo;
import com.diving.pungdong.service.image.S3Uploader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Transactional
class LectureImageServiceTest {
    @InjectMocks
    private LectureImageService lectureImageService;

    @Mock
    private LectureImageJpaRepo lectureImageJpaRepo;

    @Mock
    private S3Uploader s3Uploader;

}