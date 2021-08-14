package com.diving.pungdong.repo.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReservationJpaRepoTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    ReservationJpaRepo reservationJpaRepo;

    @Test
    @DisplayName("강의 일정이 진행중인 한 계정의 예약목록 조회")
    public void findByAccountAndAfterToday() {
        // given
        Account account = saveReservation();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> reservations = reservationJpaRepo.findByAccountAndAfterToday(account, now, pageable);

        // then
        assertThat(reservations.getContent().size()).isEqualTo(3);
    }

    private Account saveReservation() {
        Account account = Account.builder().build();
        Account savedAccount = em.persist(account);

        for (int i = 1; i <= 3; i++) {
            Reservation reservation = Reservation.builder()
                    .lastScheduleDateTime(LocalDateTime.now().minusDays(i))
                    .account(account)
                    .build();

            em.persist(reservation);
        }

        for (int i = 1; i <= 3; i++) {
            Reservation reservation = Reservation.builder()
                    .lastScheduleDateTime(LocalDateTime.now().plusDays(i))
                    .account(account)
                    .build();

            em.persist(reservation);
        }

        return savedAccount;
    }

    @Test
    @DisplayName("강의 일정이 종료된 계정의 예약목록 조회")
    public void findByAccountAndBeforeToday() {
        // given
        Account account = saveReservation();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> reservations = reservationJpaRepo.findByAccountAndBeforeToday(account, now, pageable);

        // then
        assertThat(reservations.getContent().size()).isEqualTo(3);
    }
}