package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.repo.PaymentJpaRepo;
import com.diving.pungdong.service.schedule.ScheduleEquipmentStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private ScheduleEquipmentStockService scheduleEquipmentStockService;;

    @Test
    @DisplayName("강의 대여 장비 비용 계산")
    public void calcLectureCost() {
        List<RentEquipmentInfo> rentEquipmentInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            RentEquipmentInfo rentEquipmentInfo = RentEquipmentInfo.builder()
                    .scheduleEquipmentStockId((long) i)
                    .rentNumber(4)
                    .build();
            rentEquipmentInfos.add(rentEquipmentInfo);
        }
        ScheduleEquipment scheduleEquipment = ScheduleEquipment.builder()
                .price(5000)
                .build();
        ScheduleEquipmentStock scheduleEquipmentStock = ScheduleEquipmentStock.builder()
                .scheduleEquipment(scheduleEquipment)
                .build();

        given(scheduleEquipmentStockService.findById(any())).willReturn(scheduleEquipmentStock);

        Integer equipmentRentCost = paymentService.calcEquipmentTotalRentCost(rentEquipmentInfos);

        assertThat(equipmentRentCost).isEqualTo(40000);
    }
}