package com.diving.pungdong.service.schedule;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.dto.schedule.equipment.RentEquipmentStockInfo;
import com.diving.pungdong.repo.schedule.ScheduleEquipmentStockJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleEquipmentStockService {
    private final ScheduleEquipmentStockJpaRepo scheduleEquipmentStockJpaRepo;

    public void saveScheduleEquipmentStockInfos(List<EquipmentStock> equipmentStocks, ScheduleEquipment scheduleEquipment) {
        for (EquipmentStock equipmentStock : equipmentStocks) {
            ScheduleEquipmentStock scheduleEquipmentStock = ScheduleEquipmentStock.builder()
                    .size(equipmentStock.getSize())
                    .quantity(equipmentStock.getQuantity())
                    .scheduleEquipment(scheduleEquipment)
                    .build();

            scheduleEquipmentStockJpaRepo.save(scheduleEquipmentStock);
        }
    }

    public ScheduleEquipmentStock findById(Long scheduleEquipmentStockId) {
        return scheduleEquipmentStockJpaRepo.findById(scheduleEquipmentStockId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public void updateEquipmentRentNumber(ScheduleEquipmentStock scheduleEquipmentStock, Integer rentNumber) {
        int remainingStock = scheduleEquipmentStock.getQuantity() - scheduleEquipmentStock.getTotalRentNumber();

        if (remainingStock < rentNumber) {
            throw new BadRequestException("남은 재고 수량이 없습니다");
        }

        scheduleEquipmentStock.setTotalRentNumber(scheduleEquipmentStock.getTotalRentNumber() + rentNumber);
        scheduleEquipmentStockJpaRepo.save(scheduleEquipmentStock);
    }

    @Transactional(readOnly = true)
    public List<RentEquipmentStockInfo> mapToRentEquipmentStockInfos(List<ScheduleEquipmentStock> scheduleEquipmentStocks) {
        List<RentEquipmentStockInfo> rentEquipmentStockInfos = new ArrayList<>();
        for (ScheduleEquipmentStock scheduleEquipmentStock : scheduleEquipmentStocks) {
            RentEquipmentStockInfo rentEquipmentStockInfo = RentEquipmentStockInfo.builder()
                    .scheduleEquipmentStockId(scheduleEquipmentStock.getId())
                    .size(scheduleEquipmentStock.getSize())
                    .quantity(scheduleEquipmentStock.getQuantity())
                    .totalRentNumber(scheduleEquipmentStock.getTotalRentNumber())
                    .build();

            rentEquipmentStockInfos.add(rentEquipmentStockInfo);
        }

        return rentEquipmentStockInfos;
    }
}
