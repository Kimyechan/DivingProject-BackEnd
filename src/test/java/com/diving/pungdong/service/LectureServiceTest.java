package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
import com.diving.pungdong.service.image.S3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LectureServiceTest {
    @InjectMocks
    @Spy
    private LectureService lectureService;

    @Mock
    private LectureJpaRepo lectureJpaRepo;

    @Mock
    private LectureImageService lectureImageService;

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private LectureMarkService lectureMarkService;

    @Test
    @DisplayName("강의 생성")
    public void saveLecture() {
        Lecture lecture = Lecture.builder()
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .organization(Organization.AIDA)
                .level("Level1")
                .price(100000)
                .instructor(new Account())
                .build();

        given(lectureJpaRepo.save(lecture)).willReturn(lecture);

        Lecture savedLecture = lectureService.saveLecture(lecture);

        assertThat(savedLecture.getTitle()).isEqualTo(savedLecture.getTitle());
    }

    @Test
    @DisplayName("회원이 해당 강의를 찜 했을 때")
    public void checkLectureMarked() {
        List<LectureMark> lectureMarks = new ArrayList<>();
        LectureMark lectureMark = LectureMark.builder()
                .account(Account.builder().id(1L).build())
                .lecture(Lecture.builder().id(1L).build())
                .build();
        lectureMarks.add(lectureMark);

        Account account = Account.builder()
                .id(1L)
                .build();

        given(lectureMarkService.findAllLectureMarkByAccount(account)).willReturn(lectureMarks);

        boolean result = lectureService.isLectureMarked(account, 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("회원이 해당 강의를 찜 안 했을 때")
    public void checkLectureNotMarked() {
        List<LectureMark> lectureMarks = new ArrayList<>();
        LectureMark lectureMark = LectureMark.builder()
                .account(Account.builder().id(1L).build())
                .lecture(Lecture.builder().id(1L).build())
                .build();
        lectureMarks.add(lectureMark);

        Account account = Account.builder()
                .id(1L)
                .build();

        given(lectureMarkService.findAllLectureMarkByAccount(account)).willReturn(lectureMarks);

        boolean result = lectureService.isLectureMarked(account, 2L);

        assertFalse(result);
    }

    @Test
    @DisplayName("비회원 일 때 찜 해제")
    public void markFalseWhenNotMember() {
        boolean result = lectureService.isLectureMarked(null, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("해당 강의 생성자 인지 확인 - 생성자와 불일치")
    public void checkLectureCreatorFail() {
        Long lectureId = 1L;
        Account account = Account.builder()
                .id(2L)
                .build();

        Lecture lecture = Lecture.builder()
                .instructor(Account.builder().id(1L).build())
                .build();

        doReturn(lecture).when(lectureService).findLectureById(lectureId);

        assertThrows(BadRequestException.class, () -> lectureService.checkLectureCreator(account, lectureId));
    }

    @Test
    @DisplayName("해당 강의 생성자 인지 확인 - 생성자와 일치")
    public void checkLectureCreatorSuccess() {
        Long lectureId = 1L;
        Account account = Account.builder()
                .id(2L)
                .build();

        Lecture lecture = Lecture.builder()
                .instructor(Account.builder().id(2L).build())
                .build();

        doReturn(lecture).when(lectureService).findLectureById(lectureId);

        assertDoesNotThrow(() -> lectureService.checkLectureCreator(account, lectureId));
    }

    @Test
    @DisplayName("강의의 최신 일정이 남은 날짜 계산")
    public void calcLeftScheduleDate() {
        List<ScheduleDateTime> scheduleDateTimes = new ArrayList<>();
        for (int i = 5; i < 10; i++) {
            ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                    .date(LocalDate.now().plusDays(i))
                    .build();
            scheduleDateTimes.add(scheduleDateTime);
        }

        Schedule schedule = Schedule.builder()
                .scheduleDateTimes(scheduleDateTimes)
                .build();
        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule);

        Long leftScheduleDate = lectureService.calcLeftScheduleDate(schedules);

        assertThat(leftScheduleDate).isEqualTo(5);
    }

}