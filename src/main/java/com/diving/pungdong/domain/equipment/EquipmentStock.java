package com.diving.pungdong.domain.equipment;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EquipmentStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private Equipment equipment;
}
