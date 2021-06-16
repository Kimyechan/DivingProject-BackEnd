package com.diving.pungdong.dto.schedule.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RentEquipmentInfo {
    private Long scheduleEquipmentId;

    private String name;

    private Integer price;

    private List<RentEquipmentStockInfo> stockInfoList;
}
