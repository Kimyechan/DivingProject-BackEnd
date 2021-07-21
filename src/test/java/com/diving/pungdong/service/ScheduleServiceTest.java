package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.repo.schedule.ScheduleJpaRepo;
import com.diving.pungdong.service.schedule.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Spy
    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleJpaRepo scheduleJpaRepo;

    @Test
    @DisplayName("오늘 이전 수업은 제외해서 해당 달의 강의 일정 목록 출력")
    public void filterListByCheckingPast() {
        Long lectureId = 1L;
        LocalDate currentDate = LocalDate.of(2021, 1, 15);
        List<Schedule> schedules = createSchedules(currentDate);

        doReturn(schedules).when(scheduleService).findByLectureId(lectureId);

        List<Schedule> possibleSchedule = scheduleService.findLectureScheduleByMonth(lectureId, 2021, Month.JANUARY, currentDate);

        assertThat(possibleSchedule.size()).isEqualTo(16);
    }

    @Test
    @DisplayName("같은 달 연도가 다른 일정 조회되지 않음")
    public void filterSameMonthDiffYear() {
        Long lectureId = 1L;
        LocalDate currentDate = LocalDate.of(2021, 1, 15);
        List<Schedule> schedules = createSchedules(currentDate);

        doReturn(schedules).when(scheduleService).findByLectureId(lectureId);

        List<Schedule> possibleSchedule = scheduleService.findLectureScheduleByMonth(lectureId, 2020, Month.JANUARY, currentDate);

        assertThat(possibleSchedule.size()).isEqualTo(0);
    }

    private List<Schedule> createSchedules(LocalDate currentDate) {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            List<ScheduleDateTime> dates = new ArrayList<>();
            dates.add(ScheduleDateTime.builder().date(currentDate.minusDays(i)).build());
            dates.add(ScheduleDateTime.builder().date(currentDate.minusDays(i + 1)).build());

            Schedule schedule = Schedule.builder()
                    .scheduleDateTimes(dates)
                    .build();
            schedules.add(schedule);
        }

        for (int i = 0; i < 18; i++) {
            List<ScheduleDateTime> dates = new ArrayList<>();
            dates.add(ScheduleDateTime.builder().date(currentDate.plusDays(i)).build());
            dates.add(ScheduleDateTime.builder().date(currentDate.plusDays(i + 1)).build());

            Schedule schedule = Schedule.builder()
                    .scheduleDateTimes(dates)
                    .build();
            schedules.add(schedule);
        }

        return schedules;
    }

    @Test
    @DisplayName("현재로부터 최근 강의 일정까지 남은 날짜")
    public void calcScheduleRemainingDate() {
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

        Long latestRemainingDate = scheduleService.calcScheduleRemainingDate(schedule);

        assertThat(latestRemainingDate).isEqualTo(5);
    }

    @Test
    @DisplayName("예약 신청시 신청인원 증가")
    public void plusScheduleReservationNumber() {
        Lecture lecture = Lecture.builder()
                .maxNumber(8)
                .build();

        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .currentNumber(5)
                .build();

        scheduleService.plusScheduleReservationNumber(schedule, 3);

        assertThat(schedule.getCurrentNumber()).isEqualTo(8);
    }

    @Test
    @DisplayName("예약 신청시 신청인원 초과 - 실패")
    public void plusScheduleReservationNumberFail() {
        Lecture lecture = Lecture.builder()
                .maxNumber(7)
                .build();

        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .currentNumber(5)
                .build();

        assertThrows(BadRequestException.class, () -> scheduleService.plusScheduleReservationNumber(schedule, 3));
    }

    @Test
    @DisplayName("예약 취소시 신청 인원 초기화")
    public void minusScheduleReservationNumber() {
        Schedule schedule = Schedule.builder()
                .currentNumber(5)
                .build();

        scheduleService.minusScheduleReservationNumber(schedule, 3);

        assertThat(schedule.getCurrentNumber()).isEqualTo(2);
    }
}