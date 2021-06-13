package com.diving.pungdong.service.schedule;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import com.diving.pungdong.repo.schedule.ScheduleEquipmentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleEquipmentService {
    private final ScheduleEquipmentJpaRepo scheduleEquipmentJpaRepo;
    private final ScheduleEquipmentStockService scheduleEquipmentStockService;

    public void saveScheduleEquipmentInfos(Schedule savedSchedule, List<Equipment> equipmentList) {
        for (Equipment equipment : equipmentList) {
            ScheduleEquipment scheduleEquipment = ScheduleEquipment.builder()
                    .name(equipment.getName())
                    .price(equipment.getPrice())
                    .schedule(savedSchedule)
                    .build();
            scheduleEquipmentJpaRepo.save(scheduleEquipment);

            scheduleEquipmentStockService.saveScheduleEquipmentStockInfos(equipment.getEquipmentStocks(), scheduleEquipment);
        }
    }
}