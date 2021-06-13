package com.diving.pungdong.repo.schedule;

import com.diving.pungdong.domain.schedule.ScheduleEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEquipmentJpaRepo extends JpaRepository<ScheduleEquipment, Long> {
}
