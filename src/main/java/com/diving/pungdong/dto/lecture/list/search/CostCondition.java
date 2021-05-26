package com.diving.pungdong.dto.lecture.list.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CostCondition {
    private Integer max;
    private Integer min;
}
