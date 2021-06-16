package com.diving.pungdong.domain.payment;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id @GeneratedValue
    private Long id;

    private Integer lectureCost;

    private Integer equipmentRentCost;
}
