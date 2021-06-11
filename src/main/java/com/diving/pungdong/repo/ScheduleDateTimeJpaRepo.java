package com.diving.pungdong.repo;

import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDateTimeJpaRepo extends JpaRepository<ScheduleDateTime, Long> {
}
