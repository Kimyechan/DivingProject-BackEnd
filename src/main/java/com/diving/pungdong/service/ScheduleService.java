package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.create.ScheduleDateTimeCreateInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleDateTimeInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.repo.ScheduleJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleJpaRepo scheduleJpaRepo;
    private final LectureService lectureService;
    private final ScheduleDateTimeService scheduleDateTimeService;

    public List<Schedule> findByLectureId(Long lectureId) {
        return scheduleJpaRepo.findAllByLectureId(lectureId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> findLectureScheduleByMonth(Long lectureId, Month month, LocalDate currentDate) {
        List<Schedule> schedules = findByLectureId(lectureId);

        List<Schedule> possibleMonthSchedules = new ArrayList<>();
        for (Schedule schedule : schedules) {
            List<ScheduleDateTime> scheduleDateTimes = schedule.getScheduleDateTimes();
            scheduleDateTimes.sort(Comparator.comparing(ScheduleDateTime::getDate));
            LocalDate scheduleFirstDate = scheduleDateTimes.get(0).getDate();

            if (scheduleFirstDate.isAfter(currentDate) && scheduleFirstDate.getMonth() == month) {
                possibleMonthSchedules.add(schedule);
            }
        }

        return possibleMonthSchedules;
    }

//
//    public Boolean isReservationFull(Schedule schedule, List<ReservationDateDto> reservationDateList) {
//        ReservationDateDto reservationDateDto = reservationDateList.get(0);
//
//        for (ScheduleDate scheduleDate : schedule.getScheduleDates()) {
//            if (reservationDateDto.getDate().equals(scheduleDate.getDate())) {
//                for (ScheduleTime scheduleTime : scheduleDate.getScheduleTimes()) {
//                    if (scheduleTime.getStartTime().equals(reservationDateDto.getTime()) && scheduleTime.getCurrentNumber() >= schedule.getMaxNumber()) {
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }

//    public Boolean checkValidReservationDate(List<ScheduleDate> scheduleDates, List<ReservationDateDto> reservationDateList) {
//        Integer correctCount = 0;
//        for (ReservationDateDto datetime : reservationDateList) {
//            exit_for:
//            for (ScheduleDate scheduleDate : scheduleDates) {
//                for (ScheduleTime scheduleTime : scheduleDate.getScheduleTimes()) {
//                    if (scheduleDate.getDate().equals(datetime.getDate()) && scheduleTime.getStartTime().equals(datetime.getTime())) {
//                        correctCount += 1;
//                        break exit_for;
//                    }
//                }
//            }
//        }
//
//        return correctCount.equals(scheduleDates.size());
//    }

    public Schedule getScheduleById(Long scheduleId) {
        return scheduleJpaRepo.findById(scheduleId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public Schedule saveScheduleInfo(Account account, ScheduleCreateInfo scheduleCreateInfo) {
        Lecture lecture = lectureService.getLectureById(scheduleCreateInfo.getLectureId());
        lectureService.checkLectureCreator(account, lecture.getId());

        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .maxNumber(scheduleCreateInfo.getMaxNumber())
                .build();
        Schedule savedSchedule = scheduleJpaRepo.save(schedule);

        List<ScheduleDateTime> scheduleDateTimes = scheduleDateTimeService.mapToScheduleDateTimes(scheduleCreateInfo.getDateTimeCreateInfos(), savedSchedule);
        scheduleDateTimeService.saveScheduleDateTimeList(scheduleDateTimes);

        return savedSchedule;
    }

    public List<ScheduleInfo> mapToScheduleInfos(List<Schedule> schedules) {
        List<ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (Schedule schedule : schedules) {
            List<ScheduleDateTimeInfo> scheduleDateTimeInfos = mapToScheduleDateTimeInfos(schedule.getScheduleDateTimes());

            ScheduleInfo scheduleInfo = ScheduleInfo.builder()
                    .scheduleId(schedule.getId())
                    .currentNumber(schedule.getCurrentNumber())
                    .maxNumber(schedule.getMaxNumber())
                    .dateTimeInfos(scheduleDateTimeInfos)
                    .build();
            scheduleInfos.add(scheduleInfo);
        }

        return scheduleInfos;
    }

    private List<ScheduleDateTimeInfo> mapToScheduleDateTimeInfos(List<ScheduleDateTime> scheduleDateTimes) {
        List<ScheduleDateTimeInfo> scheduleDateTimeInfos = new ArrayList<>();
        for (ScheduleDateTime scheduleDateTime : scheduleDateTimes) {
            ScheduleDateTimeInfo scheduleDateTimeInfo = ScheduleDateTimeInfo.builder()
                    .scheduleDateTimeId(scheduleDateTime.getId())
                    .startTime(scheduleDateTime.getStartTime())
                    .endTime(scheduleDateTime.getEndTime())
                    .date(scheduleDateTime.getDate())
                    .build();
            scheduleDateTimeInfos.add(scheduleDateTimeInfo);
        }

        return scheduleDateTimeInfos;
    }
}
