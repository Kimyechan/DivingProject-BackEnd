package com.diving.pungdong.repo;

import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDetailJpaRepo extends JpaRepository<ScheduleDetail, Long> {
}
