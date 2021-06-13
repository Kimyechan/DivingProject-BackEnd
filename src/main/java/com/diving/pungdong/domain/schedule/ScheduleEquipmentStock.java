package com.diving.pungdong.domain.schedule;

import lombok.*;

import javax.persistence.*;

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

    private Integer lentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleEquipment scheduleEquipment;

    @PrePersist
    public void prePersist() {
        this.lentNumber = this.lentNumber == null ? 0 : this.lentNumber;
    }
}