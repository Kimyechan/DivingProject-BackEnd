package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import com.diving.pungdong.dto.equipment.create.EquipmentStockInfo;
import com.diving.pungdong.repo.EquipmentStockJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentStockService {
    private final EquipmentStockJpaRepo equipmentStockJpaRepo;

    @Transactional
    public void saveEquipmentStock(Equipment savedEquipment, EquipmentStockInfo equipmentStockInfo) {
        EquipmentStock equipmentStock = EquipmentStock.builder()
                .size(equipmentStockInfo.getSize())
                .quantity(equipmentStockInfo.getQuantity())
                .equipment(savedEquipment)
                .build();

        equipmentStockJpaRepo.save(equipmentStock);
    }
}
