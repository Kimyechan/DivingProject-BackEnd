package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import com.diving.pungdong.repo.LectureJpaRepo;
import com.diving.pungdong.repo.SwimmingPoolJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.MapKeyColumn;
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
class LectureServiceTest {
    private LectureService lectureService;

    @Mock
    private LectureJpaRepo lectureJpaRepo;

    @Mock
    private LectureImageService lectureImageService;

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private EquipmentJpaRepo equipmentJpaRepo;

    @Mock
    private SwimmingPoolJpaRepo swimmingPoolJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lectureService = new LectureService(lectureJpaRepo, lectureImageService, s3Uploader, equipmentJpaRepo, swimmingPoolJpaRepo);
    }

    @Test
    @DisplayName("강의 생성")
    public void saveLecture() {
        Lecture lecture = Lecture.builder()
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .groupName("AIDA")
                .certificateKind("Level1")
                .price(100000)
                .period(4)
                .studentCount(5)
                .instructor(new Account())
                .swimmingPool(new SwimmingPool())
                .build();

        given(lectureJpaRepo.save(lecture)).willReturn(lecture);

        Lecture savedLecture = lectureService.saveLecture(lecture);

        assertThat(savedLecture.getTitle()).isEqualTo(savedLecture.getTitle());
    }

    @Test
    @DisplayName("강의 생성 (강의 이미지, 강의 장비 정보)")
    public void createLecture() throws IOException {
        Lecture lecture = Lecture.builder()
                .id(1L)
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .groupName("AIDA")
                .certificateKind("Level1")
                .price(100000)
                .period(4)
                .studentCount(5)
                .instructor(new Account())
                .swimmingPool(new SwimmingPool())
                .build();

        String email = "kkk@gmail.com";
        List<MultipartFile > fileList = new ArrayList<>();
        MockMultipartFile file1 = new MockMultipartFile("fileList", "test1.txt", "image/*", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("fileList", "test1.txt", "image/*", "test data".getBytes());
        fileList.add(file1);
        fileList.add(file2);

        List<Equipment> equipmentList = new ArrayList<>();
        Equipment equipment1 = Equipment.builder()
                .name("물안경")
                .price(3000)
                .build();

        Equipment equipment2 = Equipment.builder()
                .name("수영모")
                .price(3000)
                .build();

        equipmentList.add(equipment1);
        equipmentList.add(equipment2);

        given(lectureService.saveLecture(any())).willReturn(lecture);
        given(s3Uploader.upload(file1, "lecture", email)).willReturn("fil1S3UploadUrl");
        given(s3Uploader.upload(file2, "lecture", email)).willReturn("fil2S3UploadUrl");

        Lecture savedLecture = lectureService.createLecture(email, fileList, lecture, equipmentList);

        assertThat(savedLecture).isEqualTo(lecture);
        assertThat(savedLecture.getLectureImages()).isNotEmpty();

        verify(lectureImageService, times(fileList.size())).saveLectureImage(any());
    }
}