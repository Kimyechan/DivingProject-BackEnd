package com.diving.pungdong.dto.schedule.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class RentEquipmentStockInfo {
    private Long scheduleEquipmentStockId;

    private String size;

    private Integer quantity;

    private Integer totalRentNumber;
}
