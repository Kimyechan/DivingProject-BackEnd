package com.diving.pungdong.service.reservation;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.repo.PaymentJpaRepo;
import com.diving.pungdong.repo.reservation.ReservationJpaRepo;
import com.diving.pungdong.service.PaymentService;
import com.diving.pungdong.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationJpaRepo reservationJpaRepo;
    private final ScheduleService scheduleService;
    private final ReservationEquipmentService reservationEquipmentService;
    private final PaymentService paymentService;

    public Page<Reservation> findReservationListByAccount(Account account, Pageable pageable) {
        return reservationJpaRepo.findByAccount(account, pageable);
    }

    public Reservation getDetailById(Long id) {
        return reservationJpaRepo.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public void checkRightForReservation(String emailOfToken, Reservation reservation) {
        if (!emailOfToken.equals(reservation.getAccount().getEmail())) {
            throw new NoPermissionsException();
        }
    }

    public void cancelReservation(Long id) {
        reservationJpaRepo.deleteById(id);
    }

    @Transactional
    public Reservation saveReservation(Account account, ReservationCreateInfo reservationCreateInfo) {
        Schedule schedule = scheduleService.findScheduleById(reservationCreateInfo.getScheduleId());
        scheduleService.updateScheduleReservationNumber(schedule, reservationCreateInfo.getNumberOfPeople());
        Payment payment = paymentService.savePaymentInfo(schedule, reservationCreateInfo);

        Reservation reservation = Reservation.builder()
                .account(account)
                .schedule(schedule)
                .payment(payment)
                .dateOfReservation(LocalDate.now())
                .numberOfPeople(reservationCreateInfo.getNumberOfPeople())
                .build();
        Reservation savedReservation = reservationJpaRepo.save(reservation);

        reservationEquipmentService.saveReservationEquipmentList(reservationCreateInfo, savedReservation);

        return savedReservation;
    }

    public Reservation findById(Long reservationId) {
        return reservationJpaRepo.findById(reservationId).orElseThrow(ResourceNotFoundException::new);
    }
}
