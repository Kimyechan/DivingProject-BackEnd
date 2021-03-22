package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationDate;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.repo.ReservationDateJpaRepo;
import com.diving.pungdong.repo.ScheduleDetailJpaRepo;
import com.diving.pungdong.repo.ScheduleTimeJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationDateService {
    private final ReservationDateJpaRepo reservationDateJpaRepo;
    private final ScheduleDetailJpaRepo scheduleDetailJpaRepo;
    private final ScheduleTimeJpaRepo scheduleTimeJpaRepo;

    public void saveReservationDates(Reservation reservation, ReservationCreateReq req) {
        for (ReservationDateDto reservationDateDto : req.getReservationDateList()) {
            ScheduleDetail scheduleDetail = scheduleDetailJpaRepo.findById(reservationDateDto.getScheduleDetailId()).orElseThrow(ResourceNotFoundException::new);
            ScheduleTime scheduleTime = scheduleTimeJpaRepo.findById(reservationDateDto.getScheduleTimeId()).orElseThrow(ResourceNotFoundException::new);

            ReservationDate reservationDate = ReservationDate.builder()
                    .scheduleDetail(scheduleDetail)
                    .scheduleTime(scheduleTime)
                    .date(reservationDateDto.getDate())
                    .time(reservationDateDto.getTime())
                    .reservation(reservation)
                    .build();
            reservationDateJpaRepo.save(reservationDate);
        }
    }
}
