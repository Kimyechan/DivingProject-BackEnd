package com.diving.pungdong.service.reservation;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.ClosedLectureException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationEquipment;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.detail.PaymentDetail;
import com.diving.pungdong.dto.reservation.detail.RentEquipmentDetail;
import com.diving.pungdong.dto.reservation.detail.ReservationDetail;
import com.diving.pungdong.dto.reservation.detail.ScheduleDetail;
import com.diving.pungdong.dto.reservation.list.FutureReservationUIModel;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.dto.schedule.notification.Notification;
import com.diving.pungdong.repo.reservation.ReservationJpaRepo;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.PaymentService;
import com.diving.pungdong.service.kafka.ReservationKafkaProducer;
import com.diving.pungdong.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationJpaRepo reservationJpaRepo;
    private final ScheduleService scheduleService;
    private final ReservationEquipmentService reservationEquipmentService;
    private final PaymentService paymentService;
    private final LectureService lectureService;
    private final ReservationKafkaProducer reservationKafkaProducer;

    public Reservation saveReservation(Account account, ReservationCreateInfo reservationCreateInfo) {
        Schedule schedule = scheduleService.findScheduleById(reservationCreateInfo.getScheduleId());

        if (schedule.getLecture().getIsClosed()) {
            throw new ClosedLectureException("강의가 열리지 않았습니다");
        }

        scheduleService.plusScheduleReservationNumber(schedule, reservationCreateInfo.getNumberOfPeople());
        Payment payment = paymentService.savePaymentInfo(schedule, reservationCreateInfo);

        LocalDateTime lastScheduleDateTime = scheduleService.findLastScheduleDateTime(schedule);
        Reservation reservation = Reservation.builder()
                .account(account)
                .schedule(schedule)
                .payment(payment)
                .dateOfReservation(LocalDate.now())
                .lastScheduleDateTime(lastScheduleDateTime)
                .numberOfPeople(reservationCreateInfo.getNumberOfPeople())
                .build();
        Reservation savedReservation = reservationJpaRepo.save(reservation);

        reservationEquipmentService.saveReservationEquipmentList(reservationCreateInfo, savedReservation);

        reservationKafkaProducer.sendEnrollReservationEvent(
                account,
                schedule.getLecture().getInstructor(),
                schedule.getLecture(),
                schedule);

        return savedReservation;
    }

    @Transactional(readOnly = true)
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
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
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

    @Transactional(readOnly = true)
    public List<ScheduleDetail> findReservationScheduleDetail(Long reservationId) {
        Reservation reservation = findById(reservationId);

        Schedule schedule = reservation.getSchedule();

        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        for (ScheduleDateTime scheduleDateTime : schedule.getScheduleDateTimes()) {
            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .date(scheduleDateTime.getDate())
                    .startTime(scheduleDateTime.getStartTime())
                    .endTime(scheduleDateTime.getEndTime())
                    .build();
            scheduleDetails.add(scheduleDetail);
        }

        return scheduleDetails;
    }

    public void deleteReservation(Account account, Long id) {
        Reservation reservation = findById(id);
        checkRightForReservation(account, reservation.getAccount());
        checkPassFirstScheduleDate(reservation);

        Schedule schedule = reservation.getSchedule();
        scheduleService.minusScheduleReservationNumber(schedule, reservation.getNumberOfPeople());

        reservationKafkaProducer.sendCancelReservationEvent(
                account,
                schedule.getLecture().getInstructor(),
                schedule,
                schedule.getLecture());

        reservationJpaRepo.deleteById(reservation.getId());
    }

    public void checkPassFirstScheduleDate(Reservation reservation) {
        List<ScheduleDateTime> scheduleDateTimes = reservation.getSchedule().getScheduleDateTimes();
        scheduleDateTimes.sort(Comparator.comparing(ScheduleDateTime::getDate));

        LocalDate firstDate = scheduleDateTimes.get(0).getDate();
        if (firstDate.isBefore(LocalDate.now()) || firstDate.isEqual(LocalDate.now())) {
            throw new BadRequestException("예약 취소 가능한 날짜가 지났습니다");
        }
    }

    @Transactional(readOnly = true)
    public void sendNotification(Account account, Long scheduleId, Notification notification) {
        Schedule schedule = scheduleService.findScheduleById(scheduleId);

        lectureService.checkLectureCreator(account, schedule.getLecture().getId());

        List<Reservation> reservations = reservationJpaRepo.findBySchedule(schedule);
        List<String> applicantIds = new ArrayList<>();
        for (Reservation reservation : reservations) {
            applicantIds.add(String.valueOf(reservation.getAccount().getId()));
        }

        reservationKafkaProducer.sendLectureNotification(applicantIds, notification, schedule.getLecture().getId());
    }

    @Transactional(readOnly = true)
    public Page<FutureReservationUIModel> findMyFutureReservations(Account account, Pageable pageable) {
        Page<Reservation> reservationPage = reservationJpaRepo.findByAccountAndAfterToday(account, LocalDateTime.now(), pageable);

        List<FutureReservationUIModel> futureReservations = new ArrayList<>();
        for (Reservation reservation : reservationPage.getContent()) {
            Lecture lecture = reservation.getSchedule().getLecture();
            FutureReservationUIModel futureReservation = createFutureReservationUIModel(reservation, lecture);

            futureReservations.add(futureReservation);
        }

        return new PageImpl<>(futureReservations, pageable, reservationPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public FutureReservationUIModel createFutureReservationUIModel(Reservation reservation, Lecture lecture) {
        String instructorNickName = lecture.getInstructor().getNickName();
        String mainLectureImage = lectureService.findMainLectureImage(lecture);
        Long remainingDate = scheduleService.calcScheduleRemainingDate(reservation.getSchedule());

        return FutureReservationUIModel.builder()
                .reservation(reservation)
                .lecture(lecture)
                .instructorNickname(instructorNickName)
                .lectureImageUrl(mainLectureImage)
                .remainingDate(remainingDate)
                .build();
    }
}
