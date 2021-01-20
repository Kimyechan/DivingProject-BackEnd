package com.diving.pungdong.service;

import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.diving.pungdong.controller.lecture.LectureController.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentJpaRepo equipmentJpaRepo;

    public Equipment saveEquipment(Equipment equipment) {
        return equipmentJpaRepo.save(equipment);
    }

    public void lectureEquipmentUpdate(LectureUpdateInfo lectureUpdateInfo, Lecture lecture) {
        if (!lectureUpdateInfo.getEquipmentUpdateList().isEmpty()) {
            for (EquipmentUpdate equipmentUpdate : lectureUpdateInfo.getEquipmentUpdateList()) {
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
