package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ReservationFullException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationDate;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.repo.ReservationJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationJpaRepo reservationJpaRepo;
    private final ScheduleService scheduleService;
    private final ScheduleDetailService scheduleDetailService;
    private final ReservationDateService reservationDateService;

    public Reservation makeReservation(Account account, ReservationCreateReq req) {
        Schedule schedule = scheduleService.getScheduleById(req.getScheduleId());
        if (scheduleService.isReservationFull(schedule, req.getReservationDateList())) {
            throw new ReservationFullException();
        }

        Reservation reservation = saveReservation(account, req, schedule);
        reservationDateService.saveReservationDates(reservation, req);

        scheduleDetailService.plusCurrentStudentNumber(req.getReservationDateList(), schedule.getScheduleDetails());

        return reservation;
    }

    public Reservation saveReservation(Account account, ReservationCreateReq req, Schedule schedule) {
        Reservation reservation = Reservation.builder()
                .schedule(schedule)
                .account(account)
                .equipmentList(req.getEquipmentList())
                .description(req.getDescription())
                .build();

        return reservationJpaRepo.save(reservation);
    }
}
