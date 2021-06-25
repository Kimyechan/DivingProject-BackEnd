package com.diving.pungdong.dto.reservation.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RentEquipmentDetail {
    private String equipmentName;
    private String size;
    private Integer rentNumber;
}
