package com.diving.pungdong.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDateService {

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
