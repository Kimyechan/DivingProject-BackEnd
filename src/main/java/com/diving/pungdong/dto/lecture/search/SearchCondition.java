package com.diving.pungdong.dto.lecture.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCondition {
    private String groupName;
    private String certificateKind;
    private String region;
    private CostCondition costCondition;
}
