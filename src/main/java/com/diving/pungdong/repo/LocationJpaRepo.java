package com.diving.pungdong.repo;

import com.diving.pungdong.domain.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocationJpaRepo extends JpaRepository<Location, Long> {
    @Query("select l from Location l join fetch l.lecture where l.lecture.id = :lectureId")
    Optional<Location> findByLectureId(@Param("lectureId") Long lectureId);
}
