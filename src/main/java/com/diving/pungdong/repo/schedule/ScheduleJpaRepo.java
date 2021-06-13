package com.diving.pungdong.repo.schedule;

import com.diving.pungdong.domain.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleJpaRepo extends JpaRepository<Schedule, Long> {

    @Query("select distinct s from Schedule s where s.lecture.id = :lectureId")
    List<Schedule> findAllByLectureId(@Param("lectureId") Long lectureId);
}
