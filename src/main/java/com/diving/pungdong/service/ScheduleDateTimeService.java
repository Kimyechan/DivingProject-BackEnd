package com.diving.pungdong.service;

import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.create.ScheduleDateTimeCreateInfo;
import com.diving.pungdong.repo.ScheduleDateTimeJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleDateTimeService {
    private final ScheduleDateTimeJpaRepo scheduleDateTimeJpaRepo;

    @Transactional
    public void saveScheduleDateTimeList(List<ScheduleDateTime> scheduleDateTimes) {
        scheduleDateTimeJpaRepo.saveAll(scheduleDateTimes);
    }

    public List<ScheduleDateTime> mapToScheduleDateTimes(List<ScheduleDateTimeCreateInfo> scheduleDateTimeCreateInfos, Schedule savedSchedule) {
        List<ScheduleDateTime> scheduleDateTimes = new ArrayList<>();
        for (ScheduleDateTimeCreateInfo dateTimeCreateInfo : scheduleDateTimeCreateInfos) {
            ScheduleDateTime scheduleDateTime = ScheduleDateTime.builder()
                    .startTime(dateTimeCreateInfo.getStartTime())
                    .endTime(dateTimeCreateInfo.getEndTime())
                    .date(dateTimeCreateInfo.getDate())
                    .schedule(savedSchedule)
                    .build();
            scheduleDateTimes.add(scheduleDateTime);
        }
        return scheduleDateTimes;
    }
}
