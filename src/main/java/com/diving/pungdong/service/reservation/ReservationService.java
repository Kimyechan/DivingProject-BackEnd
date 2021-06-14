package com.diving.pungdong.service.reservation;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationEquipment;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.repo.reservation.ReservationJpaRepo;
import com.diving.pungdong.service.schedule.ScheduleEquipmentStockService;
import com.diving.pungdong.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

//    public Page<ReservationSubInfo> findMyReservationList(Long id, Pageable pageable) {
//        Account account = accountService.findAccountById(id);
//        Page<Reservation> reservationList = findReservationListByAccount(account, pageable);
//
//        List<ReservationSubInfo> reservationSubInfoList =  mapToReservationSubInfoList(reservationList.getContent());
//
//        return new PageImpl<>(reservationSubInfoList, pageable, reservationList.getSize());
//    }

//    public List<ReservationSubInfo> mapToReservationSubInfoList(List<Reservation> reservationList) {
//        List<ReservationSubInfo> reservationSubInfoList = new ArrayList<>();
//        for (Reservation reservation : reservationList) {
//            ReservationSubInfo reservationSubInfo = mapToReservationSubInfo(reservation);
//
//            reservationSubInfoList.add(reservationSubInfo);
//        }
//        return reservationSubInfoList;
//    }

//    public ReservationSubInfo mapToReservationSubInfo(Reservation reservation) {
//        Schedule schedule = reservation.getSchedule();
//
//        return ReservationSubInfo.builder()
//                .reservationId(reservation.getId())
//                .lectureTitle(schedule.getLecture().getTitle())
//                .isMultipleCourse(schedule.getScheduleDates().size() > 1)
//                .dateOfReservation(reservation.getDateOfReservation())
//                .totalCost(reservation.getPayment().getCost())
//                .build();
//    }

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
        checkNumberOfRemainingReservation(schedule, reservationCreateInfo.getNumberOfPeople());

        Reservation reservation = Reservation.builder()
                .account(account)
                .schedule(schedule)
                .dateOfReservation(LocalDate.now())
                .numberOfPeople(reservationCreateInfo.getNumberOfPeople())
                .build();
        Reservation savedReservation = reservationJpaRepo.save(reservation);

        reservationEquipmentService.saveReservationEquipmentList(reservationCreateInfo, savedReservation);

        return savedReservation;
    }

    public void checkNumberOfRemainingReservation(Schedule schedule, Integer numberOfPeople) {
        int numberOfRemaining = schedule.getLecture().getMaxNumber() - schedule.getCurrentNumber();

        if (numberOfRemaining < numberOfPeople) {
            throw new BadRequestException("수강 신청 인원 수 제한되었습니다.");
        }
    }

//    public List<ReservationInfo> getReservationForSchedule(Long scheduleTimeId) {
//        ScheduleTime scheduleTime = scheduleTimeService.getScheduleTimeById(scheduleTimeId);
//        List<ReservationInfo> reservationInfos = mapToReservationInfos(scheduleTime);
//
//        return reservationInfos;
//    }

//    public List<ReservationInfo> mapToReservationInfos(ScheduleTime scheduleTime) {
//        List<ReservationInfo> reservationInfos = new ArrayList<>();
//
//        for (ReservationDate reservationDate : scheduleTime.getReservationDates()) {
//            Reservation reservation = reservationDate.getReservation();
//
//            ReservationInfo reservationInfo = ReservationInfo.builder()
//                    .userName(reservation.getAccount().getNickName())
//                    .equipmentList(reservation.getEquipmentList())
//                    .description(reservation.getDescription())
//                    .build();
//
//            reservationInfos.add(reservationInfo);
//        }
//
//        return reservationInfos;
//    }
}
