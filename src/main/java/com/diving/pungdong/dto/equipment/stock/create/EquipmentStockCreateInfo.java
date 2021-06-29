package com.diving.pungdong.dto.equipment.stock.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipmentStockCreateInfo {
    @NotNull
    private Long equipmentId;

    @NotEmpty
    private String size;

    @NotNull
    private Integer quantity;
}
