package com.diving.pungdong.service.schedule;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.equipment.RentEquipmentInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleDateTimeInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.repo.schedule.ScheduleJpaRepo;
import com.diving.pungdong.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleJpaRepo scheduleJpaRepo;
    private final LectureService lectureService;
    private final ScheduleDateTimeService scheduleDateTimeService;
    private final ScheduleEquipmentService scheduleEquipmentService;

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

    public Schedule findScheduleById(Long scheduleId) {
        return scheduleJpaRepo.findById(scheduleId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public Schedule saveScheduleInfo(Account account, ScheduleCreateInfo scheduleCreateInfo) {
        Lecture lecture = lectureService.findLectureById(scheduleCreateInfo.getLectureId());
        lectureService.checkLectureCreator(account, lecture.getId());

        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .build();
        Schedule savedSchedule = scheduleJpaRepo.save(schedule);

        List<ScheduleDateTime> scheduleDateTimes = scheduleDateTimeService.mapToScheduleDateTimes(scheduleCreateInfo.getDateTimeCreateInfos(), savedSchedule);
        scheduleDateTimeService.saveScheduleDateTimeList(scheduleDateTimes);
        scheduleEquipmentService.saveScheduleEquipmentInfos(savedSchedule, lecture.getEquipmentList());

        return savedSchedule;
    }

    @Transactional(readOnly = true)
    public List<ScheduleInfo> mapToScheduleInfos(List<Schedule> schedules) {
        List<ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (Schedule schedule : schedules) {
            List<ScheduleDateTimeInfo> scheduleDateTimeInfos = mapToScheduleDateTimeInfos(schedule.getScheduleDateTimes());
            Integer maxNumber = schedule.getLecture().getMaxNumber();

            ScheduleInfo scheduleInfo = ScheduleInfo.builder()
                    .scheduleId(schedule.getId())
                    .currentNumber(schedule.getCurrentNumber())
                    .maxNumber(maxNumber)
                    .dateTimeInfos(scheduleDateTimeInfos)
                    .build();
            scheduleInfos.add(scheduleInfo);
        }

        return scheduleInfos;
    }

    @Transactional(readOnly = true)
    public List<ScheduleDateTimeInfo> mapToScheduleDateTimeInfos(List<ScheduleDateTime> scheduleDateTimes) {
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

    @Transactional
    public void updateScheduleReservationNumber(Schedule schedule, Integer numberOfPeople) {
        int numberOfRemaining = schedule.getLecture().getMaxNumber() - schedule.getCurrentNumber();

        if (numberOfRemaining < numberOfPeople) {
            throw new BadRequestException("수강 신청 인원 수를 초과 하였습니다.");
        }

        schedule.setCurrentNumber(schedule.getCurrentNumber() + numberOfPeople);
        scheduleJpaRepo.save(schedule);
    }

    public List<RentEquipmentInfo> findScheduleEquipments(Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);
        List<RentEquipmentInfo> rentEquipmentInfos = scheduleEquipmentService.mapToRentEquipmentInfo(schedule.getScheduleEquipments());

        return rentEquipmentInfos;
    }

    @Transactional(readOnly = true)
    public Long calcScheduleRemainingDate(Schedule schedule) {
        Long latestRemainingDate = 365L;
        for (ScheduleDateTime scheduleDateTime : schedule.getScheduleDateTimes()) {
            if (scheduleDateTime.getDate().isAfter(LocalDate.now()) || scheduleDateTime.getDate().isEqual(LocalDate.now())) {
                if (latestRemainingDate > ChronoUnit.DAYS.between(LocalDate.now(), scheduleDateTime.getDate())) {
                    latestRemainingDate = ChronoUnit.DAYS.between(LocalDate.now(), scheduleDateTime.getDate());
                }
            }
        }

        return latestRemainingDate;
    }
}
