package com.diving.pungdong.service.reservation;

import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationEquipment;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.detail.PaymentDetail;
import com.diving.pungdong.dto.reservation.detail.RentEquipmentDetail;
import com.diving.pungdong.dto.reservation
        .detail.ReservationDetail;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.repo.reservation.ReservationJpaRepo;
import com.diving.pungdong.service.PaymentService;
import com.diving.pungdong.service.schedule.ScheduleService;
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
    private final ReservationEquipmentService reservationEquipmentService;
    private final PaymentService paymentService;

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

    @Transactional(readOnly = true)
    public Page<ReservationInfo> findMyReservations(Account account, Pageable pageable) {
        Page<Reservation> reservationPage = reservationJpaRepo.findByAccount(account, pageable);

        List<ReservationInfo> reservationInfos = new ArrayList<>();
        for (Reservation reservation : reservationPage.getContent()) {
            Long remainingDate = scheduleService.calcScheduleRemainingDate(reservation.getSchedule());
            Lecture lecture = reservation.getSchedule().getLecture();

            ReservationInfo reservationInfo = ReservationInfo.builder()
                    .reservationId(reservation.getId())
                    .reservationDate(reservation.getDateOfReservation())
                    .instructorNickname(lecture.getInstructor().getNickName())
                    .lectureTitle(lecture.getTitle())
                    .lectureImageUrl(lecture.getLectureImages().get(0).getFileURI())
                    .remainingDate(remainingDate)
                    .build();
            reservationInfos.add(reservationInfo);
        }

        return new PageImpl<>(reservationInfos, pageable, reservationPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReservationDetail findMyReservationDetail(Account account, Long reservationId) {
        Reservation reservation = findById(reservationId);
        checkRightForReservation(account, reservation.getAccount());

        Payment payment = reservation.getPayment();
        PaymentDetail paymentDetail = PaymentDetail.builder()
                .lectureCost(payment.getLectureCost())
                .equipmentRentCost(payment.getEquipmentRentCost())
                .build();

        return ReservationDetail.builder()
                .reservationId(reservation.getId())
                .dateOfReservation(reservation.getDateOfReservation())
                .numberOfPeople(reservation.getNumberOfPeople())
                .paymentDetail(paymentDetail)
                .build();
    }

    public void checkRightForReservation(Account account, Account owner) {
        if (!account.getId().equals(owner.getId())) {
            throw new NoPermissionsException();
        }
    }

    @Transactional(readOnly = true)
    public List<RentEquipmentDetail> findRentEquipments(Long reservationId) {
        Reservation reservation = findById(reservationId);

        List<RentEquipmentDetail> rentEquipmentDetails = new ArrayList<>();
        for (ReservationEquipment reservationEquipment : reservation.getReservationEquipmentList()) {
            ScheduleEquipmentStock scheduleEquipmentStock = reservationEquipment.getScheduleEquipmentStock();
            ScheduleEquipment scheduleEquipment = scheduleEquipmentStock.getScheduleEquipment();

            RentEquipmentDetail rentEquipmentDetail = RentEquipmentDetail.builder()
                    .equipmentName(scheduleEquipment.getName())
                    .size(scheduleEquipmentStock.getSize())
                    .rentNumber(reservationEquipment.getRentNumber())
                    .build();

            rentEquipmentDetails.add(rentEquipmentDetail);
        }

        return rentEquipmentDetails;
    }
}
