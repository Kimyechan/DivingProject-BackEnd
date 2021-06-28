package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.dto.equipment.create.*;
import com.diving.pungdong.dto.lecture.update.EquipmentUpdate;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EquipmentService {
    private final EquipmentJpaRepo equipmentJpaRepo;
    private final LectureService lectureService;
    private final EquipmentStockService equipmentStockService;

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

    @Transactional
    public EquipmentCreateResult saveRentEquipmentInfos(Account account, EquipmentCreateInfo equipmentCreateInfo) {
        lectureService.checkLectureCreator(account, equipmentCreateInfo.getLectureId());
        Lecture lecture = lectureService.findLectureById(equipmentCreateInfo.getLectureId());

        List<EquipmentResult> equipmentResults = new ArrayList<>();
        for (EquipmentInfo equipmentInfo : equipmentCreateInfo.getEquipmentInfos()) {
            Equipment savedEquipment = saveEquipmentWithLecture(lecture, equipmentInfo);
            for (EquipmentStockInfo equipmentStockInfo : equipmentInfo.getEquipmentStockInfos()) {
                equipmentStockService.saveEquipmentStock(savedEquipment, equipmentStockInfo);
            }

            EquipmentResult equipmentResult = EquipmentResult.builder()
                    .equipmentId(savedEquipment.getId())
                    .name(savedEquipment.getName())
                    .build();
            equipmentResults.add(equipmentResult);
        }

        return EquipmentCreateResult.builder()
                .lectureId(lecture.getId())
                .equipmentResults(equipmentResults)
                .build();
    }

    @Transactional
    public Equipment saveEquipmentWithLecture(Lecture lecture, EquipmentInfo equipmentInfo) {
        Equipment equipment = Equipment.builder()
                .name(equipmentInfo.getName())
                .price(equipmentInfo.getPrice())
                .lecture(lecture)
                .build();

        return equipmentJpaRepo.save(equipment);
    }
}
