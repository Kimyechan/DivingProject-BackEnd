package com.diving.pungdong.service;

import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.repo.ScheduleTimeJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ScheduleTimeServiceTest {

    @InjectMocks
    private ScheduleTimeService scheduleTimeService;

    @Mock
    private ScheduleTimeJpaRepo scheduleTimeJpaRepo;

    @Test
    @DisplayName("강의 예약 인원 수 증가값 업데이트")
    public void updatePlusCurrentNumber() {
        ScheduleTime scheduleTime = ScheduleTime.builder()
                .currentNumber(4)
                .build();

        scheduleTimeService.updatePlusCurrentNumber(scheduleTime);

        assertThat(scheduleTime.getCurrentNumber()).isEqualTo(5);
    }
}