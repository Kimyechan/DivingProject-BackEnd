package com.diving.pungdong.dto.equipment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EquipmentStockDto {
    private Long id;

    private String size;

    private Integer quantity;
}
