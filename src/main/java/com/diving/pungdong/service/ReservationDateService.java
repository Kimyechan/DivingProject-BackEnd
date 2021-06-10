package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationDate;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.repo.ReservationDateJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationDateService {
    private final ReservationDateJpaRepo reservationDateJpaRepo;

//    public void saveReservationDates(Reservation reservation, ReservationCreateReq req) {
//        for (ReservationDateDto reservationDateDto : req.getReservationDateList()) {
//            ScheduleDate scheduleDate = scheduleDetailJpaRepo.findById(reservationDateDto.getScheduleDetailId()).orElseThrow(ResourceNotFoundException::new);
//            ScheduleTime scheduleTime = scheduleTimeJpaRepo.findById(reservationDateDto.getScheduleTimeId()).orElseThrow(ResourceNotFoundException::new);
//
//            ReservationDate reservationDate = ReservationDate.builder()
//                    .scheduleDate(scheduleDate)
//                    .scheduleTime(scheduleTime)
//                    .date(reservationDateDto.getDate())
//                    .time(reservationDateDto.getTime())
//                    .reservation(reservation)
//                    .build();
//            reservationDateJpaRepo.save(reservationDate);
//        }
//    }
}
