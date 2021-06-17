package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 작성이 가능한 기간인지 확인합니다 - 강의 기간 날짜가 지나지 않음")
    public void checkPossibleDateFail1() {
        List<ScheduleDateTime> scheduleDateTimeList = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now().plusDays(1))
                .endTime(LocalTime.now())
                .build();
        scheduleDateTimeList.add(scheduleDateTime);

        assertThrows(BadRequestException.class, () -> reviewService.checkPossibleDate(scheduleDateTimeList));
    }

    @Test
    @DisplayName("리뷰 작성이 가능한 기간인지 확인합니다 - 강의 기간 날짜는 동일하고 강의 종료 시간이 지나지 않음")
    public void checkPossibleDateFail2() {
        List<ScheduleDateTime> scheduleDateTimeList = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now())
                .endTime(LocalTime.now().plusHours(1))
                .build();
        scheduleDateTimeList.add(scheduleDateTime);

        assertThrows(BadRequestException.class, () -> reviewService.checkPossibleDate(scheduleDateTimeList));
    }

    @Test
    @DisplayName("리뷰 작성이 가능한 기간인지 확인합니다")
    public void checkPossibleDateSuccess() {
        List<ScheduleDateTime> scheduleDateTimeList = new ArrayList<>();
        ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                .date(LocalDate.now().minusDays(1))
                .endTime(LocalTime.now().minusHours(1))
                .build();
        scheduleDateTimeList.add(scheduleDateTime);

        assertDoesNotThrow(() -> reviewService.checkPossibleDate(scheduleDateTimeList));
    }
}