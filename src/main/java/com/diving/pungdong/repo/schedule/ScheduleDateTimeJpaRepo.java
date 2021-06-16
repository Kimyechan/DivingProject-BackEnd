package com.diving.pungdong.repo.schedule;

import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDateTimeJpaRepo extends JpaRepository<ScheduleDateTime, Long> {
}
