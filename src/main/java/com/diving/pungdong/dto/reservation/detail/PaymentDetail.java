package com.diving.pungdong.dto.reservation.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PaymentDetail {
    private Integer lectureCost;
    private Integer equipmentRentCost;
}
