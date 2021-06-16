package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentEquipmentInfo {
    private Long scheduleEquipmentStockId;
    private Integer rentNumber;
}
