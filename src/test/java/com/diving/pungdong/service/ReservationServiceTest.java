package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.reservation.list.FutureReservationUIModel;
import com.diving.pungdong.dto.reservation.list.PastReservationUIModel;
import com.diving.pungdong.service.reservation.ReservationService;
import com.diving.pungdong.service.schedule.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Spy
    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private LectureService lectureService;

    @Mock
    private ScheduleService scheduleService;

    @Test
    @DisplayName("예약한 일정에 당일 취소 - 실패")
    public void checkPassFirstScheduleDateFail1() {
        List<ScheduleDateTime> scheduleDateTimes = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now())
                .build();
        scheduleDateTimes.add(scheduleDateTime);

        Reservation reservation = Reservation.builder()
                .schedule(Schedule.builder()
                        .scheduleDateTimes(scheduleDateTimes)
                        .build())
                .build();

        assertThrows(BadRequestException.class, () -> reservationService.checkPassFirstScheduleDate(reservation));
    }

    @Test
    @DisplayName("예약한 일정 이후에 취소 - 실패")
    public void checkPassFirstScheduleDateFail2() {
        List<ScheduleDateTime> scheduleDateTimes = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now().minusDays(1))
                .build();
        scheduleDateTimes.add(scheduleDateTime);

        Reservation reservation = Reservation.builder()
                .schedule(Schedule.builder()
                        .scheduleDateTimes(scheduleDateTimes)
                        .build())
                .build();

        assertThrows(BadRequestException.class, () -> reservationService.checkPassFirstScheduleDate(reservation));
    }

    @Test
    @DisplayName("예약 취소 가능한 날짜 지나지 않음")
    public void checkPassFirstScheduleDateSuccess() {
        List<ScheduleDateTime> scheduleDateTimes = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now().plusDays(1))
                .build();
        scheduleDateTimes.add(scheduleDateTime);

        Reservation reservation = Reservation.builder()
                .schedule(Schedule.builder()
                        .scheduleDateTimes(scheduleDateTimes)
                        .build())
                .build();

        assertDoesNotThrow(() -> reservationService.checkPassFirstScheduleDate(reservation));
    }

    @Test
    @DisplayName("다가올 예약 목록 정보 중 예약 정보 한 개 만들기")
    public void createFutureReservationUIModel() {
        // given
        Account instructor = Account.builder().nickName("강사 닉네임").build();

        Lecture lecture = Lecture.builder()
                .title("강의 제목")
                .organization(Organization.AIDA)
                .level("Level1")
                .instructor(instructor)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .dateOfReservation(LocalDate.of(2021, 3, 21))
                .build();

        given(lectureService.findMainLectureImage(any())).willReturn("강의 메인 이미지 링크");
        given(scheduleService.calcScheduleRemainingDate(any())).willReturn(10L);

        // when
        FutureReservationUIModel futureReservation = reservationService.createFutureReservationUIModel(reservation, lecture);

        // then
        assertThat(futureReservation.getReservationId()).isEqualTo(reservation.getId());
        assertThat(futureReservation.getReservationDate()).isEqualTo(reservation.getDateOfReservation());
        assertThat(futureReservation.getLectureTitle()).isEqualTo(lecture.getTitle());
        assertThat(futureReservation.getOrganization()).isEqualTo(lecture.getOrganization());
        assertThat(futureReservation.getLevel()).isEqualTo(lecture.getLevel());
        assertThat(futureReservation.getInstructorNickname()).isEqualTo(lecture.getInstructor().getNickName());
        assertThat(futureReservation.getLectureImageUrl()).isEqualTo("강의 메인 이미지 링크");
        assertThat(futureReservation.getRemainingDate()).isEqualTo(10L);
    }

    @Test
    @DisplayName("지나간 예약 목록 정보 중 예약 정보 한 개 만들기")
    public void createPastReservationUIModel() {
        // given
        Account instructor = Account.builder().nickName("강사 닉네임").build();

        Lecture lecture = Lecture.builder()
                .title("강의 제목")
                .organization(Organization.AIDA)
                .level("Level1")
                .instructor(instructor)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .dateOfReservation(LocalDate.of(2021, 3, 21))
                .build();

        given(lectureService.findMainLectureImage(any())).willReturn("강의 메인 이미지 링크");
        doReturn(false).when(reservationService).checkExistedReview(any());

        // when
        PastReservationUIModel pastReservation = reservationService.createPastReservationUIModel(reservation, lecture);

        // then
        assertThat(pastReservation.getReservationId()).isEqualTo(reservation.getId());
        assertThat(pastReservation.getReservationDate()).isEqualTo(reservation.getDateOfReservation());
        assertThat(pastReservation.getLectureTitle()).isEqualTo(lecture.getTitle());
        assertThat(pastReservation.getOrganization()).isEqualTo(lecture.getOrganization());
        assertThat(pastReservation.getLevel()).isEqualTo(lecture.getLevel());
        assertThat(pastReservation.getInstructorNickname()).isEqualTo(lecture.getInstructor().getNickName());
        assertThat(pastReservation.getLectureImageUrl()).isEqualTo("강의 메인 이미지 링크");
        assertThat(pastReservation.getIsExistedReview()).isEqualTo(false);
    }

    @Test
    @DisplayName("강의 예약 리뷰 존재 여부 확인")
    public void checkExistedReview() {
        // given
        Reservation reservation = Reservation.builder()
                .review(Review.builder().build())
                .build();

        // when
        Boolean isExistedReview = reservationService.checkExistedReview(reservation);

        // then
        assertTrue(isExistedReview);
    }
}