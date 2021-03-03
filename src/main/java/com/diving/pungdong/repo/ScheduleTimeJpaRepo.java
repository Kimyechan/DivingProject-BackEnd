package com.diving.pungdong.repo;

import com.diving.pungdong.domain.schedule.ScheduleTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleTimeJpaRepo extends JpaRepository<ScheduleTime, Long> {
}
