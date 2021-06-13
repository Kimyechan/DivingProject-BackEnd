package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.reservation.ReservationEquipment;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEquipmentStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;

    private Integer quantity;

    private Integer totalRentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleEquipment scheduleEquipment;

    @OneToMany(mappedBy = "scheduleEquipmentStock", fetch = FetchType.LAZY)
    private List<ReservationEquipment> reservationEquipmentList;

    @PrePersist
    public void prePersist() {
        this.totalRentNumber = this.totalRentNumber == null ? 0 : this.totalRentNumber;
    }
}