package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.dto.lecture.update.EquipmentUpdate;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentJpaRepo equipmentJpaRepo;

    public Equipment saveEquipment(Equipment equipment) {
        return equipmentJpaRepo.save(equipment);
    }

    public void lectureEquipmentUpdate(List<EquipmentUpdate> equipmentUpdateList, Lecture lecture) {
        if (!equipmentUpdateList.isEmpty()) {
            for (EquipmentUpdate equipmentUpdate : equipmentUpdateList) {
                Equipment persistenceEquipment = equipmentJpaRepo.findByName(equipmentUpdate.getName()).orElse(null);
                if (persistenceEquipment == null) {
                    Equipment equipment = Equipment.builder()
                            .name(equipmentUpdate.getName())
                            .price(equipmentUpdate.getPrice())
                            .lecture(lecture)
                            .build();
                    equipmentJpaRepo.save(equipment);
                }

                if (equipmentUpdate.getIsDeleted()) {
                    equipmentJpaRepo.deleteByName(equipmentUpdate.getName());
                }
            }
        }
    }
}
