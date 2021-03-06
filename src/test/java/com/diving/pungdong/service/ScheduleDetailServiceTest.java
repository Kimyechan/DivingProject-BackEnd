package com.diving.pungdong.service;

import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleDetailServiceTest {

    @InjectMocks
    private ScheduleDetailService scheduleDetailService;

    @Mock
    private ScheduleTimeService scheduleTimeService;

    @Test
    @DisplayName("일치하는 일정 및 시간의 수강생 인원 수 증가시키기")
    public void plusCurrentStudentNumber() {
        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList();
        List<ScheduleDetail> scheduleDetails = createScheduleDetails();

        scheduleDetailService.plusCurrentStudentNumber(reservationDateDtoList, scheduleDetails);

        verify(scheduleTimeService, times(3)).updatePlusCurrentNumber(any());
    }

    public List<ReservationDateDto> createReservationDateDtoList() {
        List<ReservationDateDto> reservationDateDtoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ReservationDateDto reservationDateDto = ReservationDateDto.builder()
                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
                    .time(LocalTime.of(15, 0))
                    .build();

            reservationDateDtoList.add(reservationDateDto);
        }

        return reservationDateDtoList;
    }

    public List<ScheduleDetail> createScheduleDetails() {
        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ScheduleTime scheduleTime1 = ScheduleTime.builder()
                    .currentNumber(3)
                    .startTime(LocalTime.of(13, 0))
                    .build();

            ScheduleTime scheduleTime2 = ScheduleTime.builder()
                    .currentNumber(4)
                    .startTime(LocalTime.of(15, 0))
                    .build();

            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(LocalDate.of(2021, 3, 20).plusDays(i))
                    .scheduleTimes(List.of(scheduleTime1, scheduleTime2))
                    .build();
            scheduleDetails.add(scheduleDetail);
        }

        return scheduleDetails;
    }

}