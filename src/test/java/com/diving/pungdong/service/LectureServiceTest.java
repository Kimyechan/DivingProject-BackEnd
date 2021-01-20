package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
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
import com.diving.pungdong.controller.lecture.LectureController.EquipmentUpdate;
import com.diving.pungdong.controller.lecture.LectureController.LectureImageUpdate;
import com.diving.pungdong.controller.lecture.LectureController.LectureUpdateInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private SwimmingPoolJpaRepo swimmingPoolJpaRepo;

    @Mock
    private EquipmentService equipmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lectureService = new LectureService(lectureJpaRepo, lectureImageService, s3Uploader, equipmentService, swimmingPoolJpaRepo);
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

    @Test
    @DisplayName("강의 수정")
    public void updateLecture() throws IOException {
        Location location = new Location(10.0, 10.0);
        Lecture lecture = Lecture.builder()
                .id(1L)
                .title("강의1")
                .classKind("스쿠버다이빙")
                .groupName("AIDA")
                .certificateKind("LEVEL1")
                .description("강의 설명")
                .price(300000)
                .period(4)
                .studentCount(5)
                .region("서울")
                .instructor(Account.builder().email("kkk@gmail.com").build())
                .swimmingPool(SwimmingPool.builder().location(location).build())
                .build();

        LectureUpdateInfo lectureUpdateInfo = LectureUpdateInfo.builder()
                .id(1L)
                .title("강의 제목 Update")
                .classKind("스킨 스쿠버")
                .groupName("AIDA")
                .certificateKind("LEVEL2")
                .description("강의 설명  Update")
                .price(400000)
                .period(5)
                .studentCount(6)
                .region("부산")
                .swimmingPoolLocation(new Location(20.0, 20.0))
                .build();

        Lecture updateLecture = Lecture.builder()
                .id(1L)
                .title(lectureUpdateInfo.getTitle())
                .classKind(lectureUpdateInfo.getClassKind())
                .groupName(lectureUpdateInfo.getGroupName())
                .certificateKind(lectureUpdateInfo.getCertificateKind())
                .description(lectureUpdateInfo.getDescription())
                .price(lectureUpdateInfo.getPrice())
                .period(lectureUpdateInfo.getPrice())
                .period(lectureUpdateInfo.getPeriod())
                .studentCount(lectureUpdateInfo.getStudentCount())
                .region(lectureUpdateInfo.getRegion())
                .swimmingPool(SwimmingPool.builder().location(lectureUpdateInfo.getSwimmingPoolLocation()).build())
                .build();

        MockMultipartFile file1 = new MockMultipartFile("fileList", "test1.txt", "image/*", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("fileList", "test2.txt", "image/*", "test data".getBytes());
        List<MultipartFile> multipartFiles = new ArrayList<>();
        multipartFiles.add(file1);
        multipartFiles.add(file2);

        SwimmingPool swimmingPool = SwimmingPool.builder()
                .location(lectureUpdateInfo.getSwimmingPoolLocation())
                .build();

        given(swimmingPoolJpaRepo.findByLocation(lectureUpdateInfo.getSwimmingPoolLocation())).willReturn(Optional.ofNullable(swimmingPool));
        assert swimmingPool != null;
        given(swimmingPoolJpaRepo.save(swimmingPool)).willReturn(swimmingPool);
        given(lectureJpaRepo.save(any())).willReturn(updateLecture);
        Lecture returnLecture = lectureService.updateLecture(lecture.getInstructor().getEmail(), lectureUpdateInfo, multipartFiles, lecture);

        assertThat(returnLecture).isNotNull();
    }
}