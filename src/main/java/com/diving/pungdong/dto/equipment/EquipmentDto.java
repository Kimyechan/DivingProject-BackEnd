package com.diving.pungdong.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
@Builder
public class EquipmentDto {
    private Long id;

    private String name;

    private Integer price;

    private List<EquipmentStockDto> equipmentStocks;
}
