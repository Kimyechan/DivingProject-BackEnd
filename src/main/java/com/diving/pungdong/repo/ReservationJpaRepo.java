package com.diving.pungdong.repo;

import com.diving.pungdong.domain.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepo extends JpaRepository<Reservation, Long> {
}
