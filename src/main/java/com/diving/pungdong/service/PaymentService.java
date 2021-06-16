package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.repo.PaymentJpaRepo;
import com.diving.pungdong.service.schedule.ScheduleEquipmentStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentJpaRepo paymentJpaRepo;
    private final ScheduleEquipmentStockService scheduleEquipmentStockService;

    public Payment savePaymentInfo(Schedule schedule, ReservationCreateInfo reservationCreateInfo) {
        Integer lectureCost = calcLectureCost(schedule.getLecture().getPrice(), reservationCreateInfo.getNumberOfPeople());
        Integer equipmentRentPrice = calcEquipmentTotalRentCost(reservationCreateInfo.getRentEquipmentInfos());

        Payment payment = Payment.builder()
                .lectureCost(lectureCost)
                .equipmentRentCost(equipmentRentPrice)
                .build();

        return paymentJpaRepo.save(payment);
    }

    public Integer calcEquipmentTotalRentCost(List<RentEquipmentInfo> rentEquipmentInfos) {
        Integer equipmentRentPrice = 0;
        for (RentEquipmentInfo rentEquipmentInfo : rentEquipmentInfos) {
            Integer lentNumber = rentEquipmentInfo.getRentNumber();
            ScheduleEquipmentStock scheduleEquipmentStock = scheduleEquipmentStockService.findById(rentEquipmentInfo.getScheduleEquipmentStockId());

            equipmentRentPrice += lentNumber * scheduleEquipmentStock.getScheduleEquipment().getPrice();
        }
        return equipmentRentPrice;
    }

    public Integer calcLectureCost(Integer lecturePricePerPerson, Integer numberOfPeople) {
        return lecturePricePerPerson * numberOfPeople;
    }
}
