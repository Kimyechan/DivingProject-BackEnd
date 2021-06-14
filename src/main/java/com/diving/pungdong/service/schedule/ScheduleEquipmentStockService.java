package com.diving.pungdong.service.schedule;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import com.diving.pungdong.repo.schedule.ScheduleEquipmentStockJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
