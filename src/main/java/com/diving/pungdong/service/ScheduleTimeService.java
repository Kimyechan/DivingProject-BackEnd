package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleTimeService {

//    public ScheduleTime updatePlusCurrentNumber(ScheduleTime scheduleTime) {
//        scheduleTime.setCurrentNumber(scheduleTime.getCurrentNumber() + 1);
//        return scheduleTimeJpaRepo.save(scheduleTime);
//    }
//
//    public ScheduleTime getScheduleTimeById(Long scheduleTimeId) {
//        return scheduleTimeJpaRepo.findById(scheduleTimeId).orElseThrow(ResourceNotFoundException::new);
//    }
}
