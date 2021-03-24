package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReservationJpaRepo extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByAccount(Account account, Pageable pageable);
}
