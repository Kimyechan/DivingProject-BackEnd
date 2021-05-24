package com.diving.pungdong.dto.equipment.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentStockInfo {
    @NotNull
    private String size;
    @NotNull
    private Integer quantity;
}
