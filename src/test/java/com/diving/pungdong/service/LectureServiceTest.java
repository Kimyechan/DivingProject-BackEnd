package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
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
import java.time.LocalDate;
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
    private EquipmentService equipmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lectureService = new LectureService(lectureJpaRepo, lectureImageService, s3Uploader, equipmentService);
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
                .instructor(new Account())
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
                .instructor(new Account())
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
        Lecture lecture = Lecture.builder()
                .id(1L)
                .title("강의1")
                .classKind("스쿠버다이빙")
                .groupName("AIDA")
                .certificateKind("LEVEL1")
                .description("강의 설명")
                .price(300000)
                .region("서울")
                .instructor(Account.builder().email("kkk@gmail.com").build())
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
                .build();

        Lecture updateLecture = Lecture.builder()
                .id(1L)
                .title(lectureUpdateInfo.getTitle())
                .classKind(lectureUpdateInfo.getClassKind())
                .groupName(lectureUpdateInfo.getGroupName())
                .certificateKind(lectureUpdateInfo.getCertificateKind())
                .description(lectureUpdateInfo.getDescription())
                .price(lectureUpdateInfo.getPrice())
                .region(lectureUpdateInfo.getRegion())
                .build();

        given(lectureJpaRepo.save(any())).willReturn(updateLecture);
        Lecture returnLecture = lectureService.updateLecture(lectureUpdateInfo, lecture);

        assertThat(returnLecture).isNotNull();
    }

    @Test
    @DisplayName("강의 일정중 14일 이내에 있는 강의 갯수 조회")
    public void countUpcomingSchedule() {
        List<Schedule> schedules = new ArrayList<>();

        List<ScheduleDetail> scheduleDetails1 = createScheduleDetails(LocalDate.now());
        Schedule schedule1 = Schedule.builder()
                .scheduleDetails(scheduleDetails1)
                .build();
        schedules.add(schedule1);

        List<ScheduleDetail> scheduleDetails2 = createScheduleDetails(LocalDate.now().plusDays(15));
        Schedule schedule2 = Schedule.builder()
                .scheduleDetails(scheduleDetails2)
                .build();
        schedules.add(schedule2);

        Lecture lecture = Lecture.builder()
                .schedules(schedules)
                .build();

        Integer upcomingScheduleCount = lectureService.countUpcomingSchedule(lecture);

        assertThat(upcomingScheduleCount).isEqualTo(1);
    }

    public List<ScheduleDetail> createScheduleDetails(LocalDate startDate) {
        List<ScheduleDetail> scheduleDetails = new ArrayList<>();

        ScheduleDetail scheduleDetail1 = ScheduleDetail.builder()
                .date(startDate)
                .build();

        ScheduleDetail scheduleDetail2 = ScheduleDetail.builder()
                .date(startDate.plusDays(14))
                .build();

        scheduleDetails.add(scheduleDetail1);
        scheduleDetails.add(scheduleDetail2);

        return scheduleDetails;
    }

}