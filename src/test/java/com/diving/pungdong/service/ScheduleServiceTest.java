package com.diving.pungdong.service;

import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        List<Schedule> possibleSchedule = scheduleService.findLectureScheduleByMonth(lectureId, Month.JANUARY, currentDate);

        assertThat(possibleSchedule.size()).isEqualTo(16);
    }

    private List<Schedule> createSchedules(LocalDate currentDate) {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            List<LocalDate> dates = new ArrayList<>();
            dates.add(currentDate.minusDays(i));
            dates.add(currentDate.minusDays(i + 1));

            Schedule schedule = Schedule.builder()
                    .dates(dates)
                    .build();
            schedules.add(schedule);
        }

        for (int i = 0; i < 18; i++) {
            List<LocalDate> dates = new ArrayList<>();
            dates.add(currentDate.plusDays(i));
            dates.add(currentDate.plusDays(i + 1));

            Schedule schedule = Schedule.builder()
                    .dates(dates)
                    .build();
            schedules.add(schedule);
        }

        return schedules;
    }

//    @Test
//    @DisplayName("강의 예약 인원 수가 가득 찾는지 체크 - 가득 안 참")
//    public void isReservationFullFalse() {
//        Schedule schedule = createScheduleForReservation();
//        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(13, 0));
//
//        Boolean result = scheduleService.isReservationFull(schedule, reservationDateDtoList);
//
//        assertFalse(result);
//    }
//
//    @Test
//    @DisplayName("강의 예약 인원 수가 가득 찾는지 체크 - 가득 참")
//    public void isReservationFullTrue() {
//        Schedule schedule = createScheduleForReservation();
//        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(15, 0));
//
//        Boolean result = scheduleService.isReservationFull(schedule, reservationDateDtoList);
//
//        assertTrue(result);
//    }
//
//    public List<ReservationDateDto> createReservationDateDtoList(Schedule schedule, LocalTime time) {
//        List<ReservationDateDto> reservationDateDtoList = new ArrayList<>();
//        for (int i = 0; i < schedule.getPeriod(); i++) {
//            ReservationDateDto reservationDateDto = ReservationDateDto.builder()
//                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
//                    .time(time)
//                    .build();
//
//            reservationDateDtoList.add(reservationDateDto);
//        }
//
//        return reservationDateDtoList;
//    }
//
//    public Schedule createScheduleForReservation() {
//        Schedule schedule = Schedule.builder()
//                .maxNumber(5)
//                .period(3)
//                .build();
//
//        List<ScheduleDate> scheduleDates = new ArrayList<>();
//        for (int i = 0; i < schedule.getPeriod(); i++) {
//            ScheduleTime scheduleTime1 = ScheduleTime.builder()
//                    .currentNumber(3)
//                    .startTime(LocalTime.of(13, 0))
//                    .build();
//
//            ScheduleTime scheduleTime2 = ScheduleTime.builder()
//                    .currentNumber(5)
//                    .startTime(LocalTime.of(15, 0))
//                    .build();
//
//            ScheduleDate scheduleDate = ScheduleDate.builder()
//                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
//                    .scheduleTimes(List.of(scheduleTime1, scheduleTime2))
//                    .build();
//            scheduleDates.add(scheduleDate);
//        }
//        schedule.setScheduleDates(scheduleDates);
//
//        return schedule;
//    }
//
//    @Test
//    @DisplayName("일치하는 강의 날짜만 요청 하였는 지 체크 - 성공")
//    public void checkValidReservationDateSuccess() {
//        Schedule schedule = createScheduleForReservation();
//        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(13, 0));
//
//        Boolean result = scheduleService.checkValidReservationDate(schedule.getScheduleDates(), reservationDateDtoList);
//
//        assertTrue(result);
//    }
//
//    @Test
//    @DisplayName("일치하는 강의 날짜만 요청 하였는 지 체크 - 실패")
//    public void checkValidReservationDateFail() {
//        Schedule schedule = createScheduleForReservation();
//        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(16, 0));
//
//        Boolean result = scheduleService.checkValidReservationDate(schedule.getScheduleDates(), reservationDateDtoList);
//
//        assertFalse(result);
//    }


}