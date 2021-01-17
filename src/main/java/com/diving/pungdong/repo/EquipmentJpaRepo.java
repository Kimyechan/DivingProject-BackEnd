package com.diving.pungdong.repo;

import com.diving.pungdong.domain.equipment.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentJpaRepo extends JpaRepository<Equipment, Long> {
}
