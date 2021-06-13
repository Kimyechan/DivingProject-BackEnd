package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.schedule.Schedule;
import lombok.*;
import org.hibernate.annotations.Fetch;

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

    private Integer numberOfPeople;

    private LocalDate dateOfReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY)
    private List<ReservationEquipment> reservationEquipmentList;

    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;
}
