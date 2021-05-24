package com.diving.pungdong.repo;

import com.diving.pungdong.domain.equipment.EquipmentStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentStockJpaRepo extends JpaRepository<EquipmentStock, Long> {
}
