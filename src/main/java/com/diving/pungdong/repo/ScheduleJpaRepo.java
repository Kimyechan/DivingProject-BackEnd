package com.diving.pungdong.repo;

import com.diving.pungdong.domain.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleJpaRepo extends JpaRepository<Schedule, Long> {
}
