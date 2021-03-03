package com.diving.pungdong.dto.lecture.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentUpdate {
    private String name;
    private Integer price;
    Boolean isDeleted;
}