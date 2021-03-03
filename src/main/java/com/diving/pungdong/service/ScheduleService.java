package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateReq;
import com.diving.pungdong.dto.schedule.create.ScheduleDetailReq;
import com.diving.pungdong.repo.ScheduleDetailJpaRepo;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import com.diving.pungdong.repo.ScheduleTimeJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ScheduleJpaRepo scheduleJpaRepo;
    private final ScheduleDetailJpaRepo scheduleDetailJpaRepo;
    private final ScheduleTimeJpaRepo scheduleTimeJpaRepo;

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleJpaRepo.save(schedule);
    }

    public Schedule saveScheduleTx(Lecture lecture, ScheduleCreateReq scheduleCreateReq) {
        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .period(scheduleCreateReq.getPeriod())
                .maxNumber(scheduleCreateReq.getMaxNumber())
                .build();
        Schedule savedSchedule = saveSchedule(schedule);

        for (ScheduleDetailReq scheduleDetailReq : scheduleCreateReq.getDetailReqList()) {
            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(scheduleDetailReq.getDate())
                    .lectureTime(scheduleDetailReq.getLectureTime())
                    .location(scheduleDetailReq.getLocation())
                    .schedule(schedule)
                    .build();
            scheduleDetailJpaRepo.save(scheduleDetail);

            for (LocalTime startTime : scheduleDetailReq.getStartTimes()) {
                ScheduleTime scheduleTime = ScheduleTime.builder()
                        .currentNumber(0)
                        .startTime(startTime)
                        .scheduleDetail(scheduleDetail)
                        .build();
                scheduleTimeJpaRepo.save(scheduleTime);
            }
        }

        return savedSchedule;
    }

    public List<Schedule> getByLectureId(Long lectureId) {
        return scheduleJpaRepo.findByLectureId(lectureId);
    }

}
