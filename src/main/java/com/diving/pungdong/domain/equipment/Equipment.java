package com.diving.pungdong.domain.equipment;

import com.diving.pungdong.domain.lecture.Lecture;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Equipment {
    @Id @GeneratedValue
    private Long id;

    private String name;

    private Integer price;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EquipmentStock> equipmentStocks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;
}
