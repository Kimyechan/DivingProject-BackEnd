package com.diving.pungdong.dto.equipment.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EquipmentResult {
    private Long equipmentId;
    private String name;
}
