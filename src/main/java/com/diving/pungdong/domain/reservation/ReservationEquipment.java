package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.schedule.ScheduleEquipmentStock;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEquipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleEquipmentStock scheduleEquipmentStock;

    private Integer rentNumber;
}
