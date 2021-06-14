package com.diving.pungdong.service.reservation;

import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.repo.reservation.ReservationEquipmentJpaRepo;
import com.diving.pungdong.service.schedule.ScheduleEquipmentStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationEquipmentService {
    private final ReservationEquipmentJpaRepo reservationEquipmentJpaRepo;
    private final ScheduleEquipmentStockService scheduleEquipmentStockService;

    @Transactional
    public void saveReservationEquipmentList(ReservationCreateInfo reservationCreateInfo, Reservation savedReservation) {
        List<ReservationEquipment> reservationEquipmentList = new ArrayList<>();
        for (RentEquipmentInfo rentEquipmentInfo : reservationCreateInfo.getRentEquipmentInfos()) {
            ScheduleEquipmentStock scheduleEquipmentStock = scheduleEquipmentStockService.findById(rentEquipmentInfo.getScheduleEquipmentStockId());
            scheduleEquipmentStock.checkRemainingStock(scheduleEquipmentStock, rentEquipmentInfo.getRentNumber());

            ReservationEquipment reservationEquipment = ReservationEquipment.builder()
                    .reservation(savedReservation)
                    .scheduleEquipmentStock(scheduleEquipmentStock)
                    .rentNumber(rentEquipmentInfo.getRentNumber())
                    .build();
            reservationEquipmentList.add(reservationEquipment);
        }

        reservationEquipmentJpaRepo.saveAll(reservationEquipmentList);
    }
}
