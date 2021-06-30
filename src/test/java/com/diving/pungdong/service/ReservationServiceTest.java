package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.service.reservation.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Spy
    @InjectMocks
    private ReservationService reservationService;

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
}