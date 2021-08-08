package com.diving.pungdong.domain.schedule;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEquipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    @OneToMany(mappedBy = "scheduleEquipment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ScheduleEquipmentStock> scheduleEquipmentStocks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;
}