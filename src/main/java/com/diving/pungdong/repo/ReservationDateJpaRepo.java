package com.diving.pungdong.repo;

import com.diving.pungdong.domain.reservation.ReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDateJpaRepo extends JpaRepository<ReservationDate, Long> {
}
