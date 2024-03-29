package com.diving.pungdong.repo.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface ReservationJpaRepo extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByAccount(Account account, Pageable pageable);

    @Query("select r from Reservation r join fetch r.account where r.schedule = :schedule")
    List<Reservation> findBySchedule(@Param("schedule") Schedule schedule);

    @Query("select r from Reservation r where r.account = :account and r.lastScheduleDateTime >= :now")
    Page<Reservation> findByAccountAndAfterToday(@Param("account") Account account, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("select r from Reservation r where r.account = :account and r.lastScheduleDateTime < :now")
    Page<Reservation> findByAccountAndBeforeToday(@Param("account") Account account, @Param("now") LocalDateTime now, Pageable pageable);
}
