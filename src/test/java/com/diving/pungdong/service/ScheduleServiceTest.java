package com.diving.pungdong.service;

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

}