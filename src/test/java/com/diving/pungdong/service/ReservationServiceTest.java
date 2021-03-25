package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.reservation.ReservationSubInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Spy
    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private AccountService accountService;

    @Test
    @DisplayName("내 강의 예약 목록 조회")
    public void findMyReservationList() {
        String email = "kim@gmail.com";
        Account account = createStudentAccount();
        List<Reservation> reservationList = new ArrayList<>();

        Lecture lecture = Lecture.builder()
                .title("강의 제목")
                .build();
        ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                .build();
        Schedule schedule = Schedule.builder()
                .lecture(lecture)
                .scheduleDetails(List.of(scheduleDetail))
                .build();
        Payment payment = Payment.builder()
                .cost(100000)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .schedule(schedule)
                .payment(payment)
                .dateOfReservation(LocalDate.of(2021, 3, 4))
                .account(account)
                .build();

        reservationList.add(reservation);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Reservation> reservationPage = new PageImpl<>(reservationList, pageable, reservationList.size());

        given(accountService.findAccountByEmail(email)).willReturn(account);
        doReturn(reservationPage).when(reservationService).findReservationListByAccount(account, pageable);

        Page<ReservationSubInfo> reservationSubInfoList = reservationService.findMyReservationList(email, pageable);

        ReservationSubInfo reservationSubInfo = reservationSubInfoList.getContent().get(0);
        assertThat(reservationSubInfo.getTotalCost()).isEqualTo(payment.getCost());
        assertThat(reservationSubInfo.getDateOfReservation()).isEqualTo(reservation.getDateOfReservation());
        assertThat(reservationSubInfo.getIsMultipleCourse()).isFalse();
        assertThat(reservationSubInfo.getLectureTitle()).isEqualTo(lecture.getTitle());
    }

    public Account createStudentAccount() {
        return Account.builder()
                .id(1L)
                .email("kim@gmail.com")
                .password("1234")
                .userName("kim")
                .age(27)
                .gender(Gender.MALE)
                .roles(Set.of(Role.STUDENT))
                .build();
    }


}