package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.dto.lecture.update.LectureImageUpdate;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.repo.LectureImageJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Transactional
class LectureImageServiceTest {
    private LectureImageService lectureImageService;

    @Mock
    private LectureImageJpaRepo lectureImageJpaRepo;

    @Mock
    private S3Uploader s3Uploader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        lectureImageService = new LectureImageService(lectureImageJpaRepo, s3Uploader);
   }

    @Test
    @DisplayName("강의 사진 저장")
    public void saveLectureImage() {
        LectureImage lectureImage = LectureImage.builder()
                .fileURI("/lecture/test.png")
                .lecture(any())
                .build();

        given(lectureImageService.saveLectureImage(lectureImage)).willReturn(lectureImage);
        LectureImage savedLectureImage = lectureImageService.saveLectureImage(lectureImage);

        assertThat(savedLectureImage.getFileURI()).isEqualTo(lectureImage.getFileURI());
    }

    @Test
    @DisplayName("강의 사진 여러 장 연속 삭제 - isDeleted 체크")
    public void deleteListByFileURL() {
        LectureImageUpdate lectureImageUpdate1 = LectureImageUpdate.builder()
                .lectureImageURL("image url 1")
                .isDeleted(true)
                .build();
        LectureImageUpdate lectureImageUpdate2 = LectureImageUpdate.builder()
                .lectureImageURL("image url 2")
                .isDeleted(false)
                .build();
        LectureUpdateInfo lectureUpdateInfo = LectureUpdateInfo.builder()
                .lectureImageUpdateList(List.of(lectureImageUpdate1, lectureImageUpdate2))
                .build();

        lectureImageService.deleteIfIsDeleted(lectureUpdateInfo);

        verify(lectureImageJpaRepo, times(1)).deleteByFileURI(any());
        verify(s3Uploader, times(1)).deleteFileFromS3(any());
    }

    @Test
    @DisplayName("강의 사진 리스트 연속 저장")
    public void saveList() throws IOException {
        String email = "kyc@gmail.com";

        MockMultipartFile file1 = new MockMultipartFile("fileList", "test1.txt", "image/*", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("fileList", "test2.txt", "image/*", "test data".getBytes());
        List<MultipartFile> fileList = new ArrayList<>();
        fileList.add(file1);
        fileList.add(file2);

        Lecture lecture = new Lecture();

        lectureImageService.addList(email, fileList, lecture);

        verify(s3Uploader, times(2)).upload(any(), any(), any());
        verify(lectureImageJpaRepo, times(2)).save(any());
    }
}