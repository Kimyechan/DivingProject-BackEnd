package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import com.diving.pungdong.dto.equipment.create.EquipmentStockInfo;
import com.diving.pungdong.dto.equipment.stock.create.EquipmentStockCreateInfo;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import com.diving.pungdong.repo.EquipmentStockJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentStockService {
    private final EquipmentJpaRepo equipmentJpaRepo;
    private final EquipmentStockJpaRepo equipmentStockJpaRepo;
    private final LectureService lectureService;

    @Transactional
    public void saveEquipmentStock(Equipment savedEquipment, EquipmentStockInfo equipmentStockInfo) {
        EquipmentStock equipmentStock = EquipmentStock.builder()
                .size(equipmentStockInfo.getSize())
                .quantity(equipmentStockInfo.getQuantity())
                .equipment(savedEquipment)
                .build();

        equipmentStockJpaRepo.save(equipmentStock);
    }

    @Transactional
    public EquipmentStock createEquipmentStock(Account account, EquipmentStockCreateInfo stockCreateInfo) {
        Equipment equipment = equipmentJpaRepo.findById(stockCreateInfo.getEquipmentId()).orElseThrow(ResourceNotFoundException::new);
        lectureService.checkLectureCreator(account, equipment.getLecture().getId());

        EquipmentStock equipmentStock = EquipmentStock.builder()
                .size(stockCreateInfo.getSize())
                .quantity(stockCreateInfo.getQuantity())
                .equipment(equipment)
                .build();

        return equipmentStockJpaRepo.save(equipmentStock);
    }

    @Transactional
    public void deleteEquipmentStock(Account account, Long id) {
        EquipmentStock equipmentStock = equipmentStockJpaRepo.findById(id).orElseThrow(ResourceNotFoundException::new);
        lectureService.checkLectureCreator(account, equipmentStock.getEquipment().getLecture().getId());

        equipmentStockJpaRepo.deleteById(equipmentStock.getId());
    }
}
