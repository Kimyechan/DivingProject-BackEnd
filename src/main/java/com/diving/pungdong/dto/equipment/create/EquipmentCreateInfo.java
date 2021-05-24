package com.diving.pungdong.dto.equipment.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentCreateInfo {
    @NotNull
    private Long lectureId;
    private List<EquipmentInfo> equipmentInfos;
}
