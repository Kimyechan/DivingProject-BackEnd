package com.diving.pungdong.dto.equipment.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class EquipmentCreateResult {
    private Long lectureId;
    private List<EquipmentResult> equipmentResults;
}
