package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.schedule.Schedule;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "reservation",fetch = FetchType.LAZY)
    private List<ReservationDate> reservationDateList;

    @ElementCollection
    private List<String> equipmentList;

    private LocalDate dateOfReservation;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;
}
