package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.Location;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDate {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private LocalTime time;

    @Embedded
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;
}
