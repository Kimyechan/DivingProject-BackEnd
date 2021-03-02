package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateReq;
import com.diving.pungdong.dto.schedule.create.ScheduleDetailReq;
import com.diving.pungdong.repo.ScheduleDetailJpaRepo;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ScheduleJpaRepo scheduleJpaRepo;
    private final ScheduleDetailJpaRepo scheduleDetailJpaRepo;

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleJpaRepo.save(schedule);
    }

    public Schedule saveScheduleTx(Lecture lecture, ScheduleCreateReq scheduleCreateReq) {
        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .period(scheduleCreateReq.getPeriod())
                .build();
        Schedule savedSchedule = saveSchedule(schedule);

        for (ScheduleDetailReq scheduleDetailReq : scheduleCreateReq.getDetailReqList()) {
            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(scheduleDetailReq.getDate())
                    .startTimes(scheduleDetailReq.getStartTimes())
                    .lectureTime(scheduleDetailReq.getLectureTime())
                    .location(scheduleDetailReq.getLocation())
                    .schedule(schedule)
                    .build();
            scheduleDetailJpaRepo.save(scheduleDetail);
        }

        return savedSchedule;
    }

    public List<Schedule> getByLectureId(Long lectureId) {
        return scheduleJpaRepo.findByLectureId(lectureId);
    }

}
