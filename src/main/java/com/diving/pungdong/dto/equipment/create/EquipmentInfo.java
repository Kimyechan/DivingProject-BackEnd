package com.diving.pungdong.dto.equipment.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentInfo {
    @NotEmpty
    private String name;

    @NotNull
    private Integer price;

    private List<EquipmentStockInfo> equipmentStockInfos;
}
