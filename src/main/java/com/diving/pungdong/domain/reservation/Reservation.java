package com.diving.pungdong.domain.reservation;

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
}
