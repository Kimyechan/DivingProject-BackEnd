package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.domain.schedule.Schedule;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "reservation",fetch = FetchType.LAZY)
    private List<ReservationDate> reservationDateList;

    @ElementCollection
    private List<String> equipmentList;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;
}
