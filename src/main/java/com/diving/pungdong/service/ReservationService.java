package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.ReservationFullException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationSubInfo;
import com.diving.pungdong.repo.ReservationJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final PaymentService paymentService;
    private final AccountService accountService;

    public Reservation makeReservation(Account account, ReservationCreateReq req) {
        Schedule schedule = scheduleService.getScheduleById(req.getScheduleId());
        if (!scheduleService.checkValidReservationDate(schedule.getScheduleDetails(), req.getReservationDateList())) {
            throw new BadRequestException();
        }

        if (scheduleService.isReservationFull(schedule, req.getReservationDateList())) {
            throw new ReservationFullException();
        }

        Integer cost = paymentService.calcCost(req.getEquipmentList(), schedule.getLecture());
        Payment payment = paymentService.savePayment(cost);

        Reservation reservation = saveReservation(account, schedule, payment, req);
        reservationDateService.saveReservationDates(reservation, req);

        scheduleDetailService.plusCurrentStudentNumber(req.getReservationDateList(), schedule.getScheduleDetails());

        return reservation;
    }

    public Reservation saveReservation(Account account, Schedule schedule, Payment payment, ReservationCreateReq req) {
        Reservation reservation = Reservation.builder()
                .schedule(schedule)
                .account(account)
                .payment(payment)
                .equipmentList(req.getEquipmentList())
                .description(req.getDescription())
                .dateOfReservation(LocalDate.now())
                .build();

        return reservationJpaRepo.save(reservation);
    }

    public Page<ReservationSubInfo> findMyReservationList(String email, Pageable pageable) {
        Account account = accountService.findAccountByEmail(email);
        Page<Reservation> reservationList = findReservationListByAccount(account, pageable);

        List<ReservationSubInfo> reservationSubInfoList =  mapToReservationSubInfoList(reservationList.getContent());

        return new PageImpl<>(reservationSubInfoList, pageable, reservationList.getSize());
    }

    public List<ReservationSubInfo> mapToReservationSubInfoList(List<Reservation> reservationList) {
        List<ReservationSubInfo> reservationSubInfoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            ReservationSubInfo reservationSubInfo = mapToReservationSubInfo(reservation);

            reservationSubInfoList.add(reservationSubInfo);
        }
        return reservationSubInfoList;
    }

    public ReservationSubInfo mapToReservationSubInfo(Reservation reservation) {
        Schedule schedule = reservation.getSchedule();

        return ReservationSubInfo.builder()
                .lectureTitle(schedule.getLecture().getTitle())
                .isMultipleCourse(schedule.getScheduleDetails().size() > 1)
                .dateOfReservation(reservation.getDateOfReservation())
                .totalCost(reservation.getPayment().getCost())
                .build();
    }

    public Page<Reservation> findReservationListByAccount(Account account, Pageable pageable) {
        return reservationJpaRepo.findByAccount(account, pageable);
    }
}
