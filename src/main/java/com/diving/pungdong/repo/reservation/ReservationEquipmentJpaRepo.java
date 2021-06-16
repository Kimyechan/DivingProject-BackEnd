package com.diving.pungdong.repo.reservation;

import com.diving.pungdong.domain.reservation.ReservationEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationEquipmentJpaRepo extends JpaRepository<ReservationEquipment, Long> {
}
