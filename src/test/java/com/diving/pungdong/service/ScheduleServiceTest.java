package com.diving.pungdong.service;

import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
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
    @DisplayName("오늘 이전 수업은 제외해서 수업 리스트 출력")
    public void filterListByCheckingPast() {
        Long lectureId = 1L;
        List<Schedule> scheduleList = createSchedules();

        doReturn(scheduleList).when(scheduleService).getByLectureId(lectureId);

        List<Schedule> result = scheduleService.filterListByCheckingPast(lectureId);

        assertThat(result.size()).isEqualTo(0);
    }

    public List<Schedule> createSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        Schedule schedule = Schedule.builder()
                .period(3)
                .maxNumber(10)
                .build();

        Location location = Location.builder()
                .latitude(37.0)
                .longitude(127.0)
                .address("상세 주소")
                .build();

        List<ScheduleDetail> scheduleDetails = createPastScheduleDetails(location, schedule.getPeriod());

        schedule.setScheduleDetails(scheduleDetails);
        schedules.add(schedule);
        return schedules;
    }

    public List<ScheduleDetail> createPastScheduleDetails(Location location, Integer period) {
        List<ScheduleDetail> scheduleDetails = new ArrayList<>();

        for (int i = -1; i < period-1; i++) {
            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(LocalDate.now().plusDays(i))
                    .lectureTime(LocalTime.of(1, 30))
                    .location(location)
                    .build();
            scheduleDetails.add(scheduleDetail);
        }
        return scheduleDetails;
    }

    @Test
    @DisplayName("강의 예약 인원 수가 가득 찾는지 체크 - 가득 안 참")
    public void isReservationFullFalse() {
        Schedule schedule = createScheduleForReservation();
        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(13, 0));

        Boolean result = scheduleService.isReservationFull(schedule, reservationDateDtoList);

        assertFalse(result);
    }

    @Test
    @DisplayName("강의 예약 인원 수가 가득 찾는지 체크 - 가득 참")
    public void isReservationFullTrue() {
        Schedule schedule = createScheduleForReservation();
        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(15, 0));

        Boolean result = scheduleService.isReservationFull(schedule, reservationDateDtoList);

        assertTrue(result);
    }

    public List<ReservationDateDto> createReservationDateDtoList(Schedule schedule, LocalTime time) {
        List<ReservationDateDto> reservationDateDtoList = new ArrayList<>();
        for (int i = 0; i < schedule.getPeriod(); i++) {
            ReservationDateDto reservationDateDto = ReservationDateDto.builder()
                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
                    .time(time)
                    .build();

            reservationDateDtoList.add(reservationDateDto);
        }

        return reservationDateDtoList;
    }

    public Schedule createScheduleForReservation() {
        Schedule schedule = Schedule.builder()
                .maxNumber(5)
                .period(3)
                .build();

        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        for (int i = 0; i < schedule.getPeriod(); i++) {
            ScheduleTime scheduleTime1 = ScheduleTime.builder()
                    .currentNumber(3)
                    .startTime(LocalTime.of(13, 0))
                    .build();

            ScheduleTime scheduleTime2 = ScheduleTime.builder()
                    .currentNumber(5)
                    .startTime(LocalTime.of(15, 0))
                    .build();

            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
                    .scheduleTimes(List.of(scheduleTime1, scheduleTime2))
                    .build();
            scheduleDetails.add(scheduleDetail);
        }
        schedule.setScheduleDetails(scheduleDetails);

        return schedule;
    }

    @Test
    @DisplayName("일치하는 강의 날짜만 요청 하였는 지 체크 - 성공")
    public void checkValidReservationDateSuccess() {
        Schedule schedule = createScheduleForReservation();
        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(13, 0));

        Boolean result = scheduleService.checkValidReservationDate(schedule.getScheduleDetails(), reservationDateDtoList);

        assertTrue(result);
    }

    @Test
    @DisplayName("일치하는 강의 날짜만 요청 하였는 지 체크 - 실패")
    public void checkValidReservationDateFail() {
        Schedule schedule = createScheduleForReservation();
        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList(schedule, LocalTime.of(16, 0));

        Boolean result = scheduleService.checkValidReservationDate(schedule.getScheduleDetails(), reservationDateDtoList);

        assertFalse(result);
    }


}