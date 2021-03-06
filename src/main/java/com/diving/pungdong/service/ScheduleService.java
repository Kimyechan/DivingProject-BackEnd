package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateReq;
import com.diving.pungdong.dto.schedule.create.ScheduleDetailReq;
import com.diving.pungdong.repo.ScheduleDetailJpaRepo;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import com.diving.pungdong.repo.ScheduleTimeJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

    public List<Schedule> filterListByCheckingPast(Long lectureId) {
        List<Schedule> scheduleList = getByLectureId(lectureId);

        List<Schedule> newScheduleList = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            boolean isPast = false;

            for (ScheduleDetail scheduleDetail : schedule.getScheduleDetails()) {
                if (scheduleDetail.getDate().isBefore(LocalDate.now())) {
                    isPast = true;
                }
            }

            if (!isPast) {
                newScheduleList.add(schedule);
            }
        }

        return newScheduleList;
    }

    public Boolean isReservationFull(Schedule schedule, List<ReservationDateDto> reservationDateList) {
        ReservationDateDto reservationDateDto = reservationDateList.get(0);

        for (ScheduleDetail scheduleDetail : schedule.getScheduleDetails()) {
            if (reservationDateDto.getDate().equals(scheduleDetail.getDate())) {
                for (ScheduleTime scheduleTime : scheduleDetail.getScheduleTimes()) {
                    if (scheduleTime.getStartTime().equals(reservationDateDto.getTime()) && scheduleTime.getCurrentNumber() >= schedule.getMaxNumber()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Boolean checkValidReservationDate(List<ScheduleDetail> scheduleDetails, List<ReservationDateDto> reservationDateList) {
        Integer correctCount = 0;
        for (ReservationDateDto datetime : reservationDateList) {
            exit_for:
            for (ScheduleDetail scheduleDetail : scheduleDetails) {
                for (ScheduleTime scheduleTime : scheduleDetail.getScheduleTimes()) {
                    if (scheduleDetail.getDate().equals(datetime.getDate()) && scheduleTime.getStartTime().equals(datetime.getTime())) {
                        correctCount += 1;
                        break exit_for;
                    }
                }
            }
        }

        return correctCount.equals(scheduleDetails.size());
    }

    public Schedule getScheduleById(Long scheduleId) {
        return scheduleJpaRepo.findById(scheduleId).orElseThrow(ResourceNotFoundException::new);
    }
}
