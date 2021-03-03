package com.diving.pungdong.service;

import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
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
}