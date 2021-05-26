package com.diving.pungdong.dto.lecture.list.search;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterSearchCondition {
    private Organization organization;
    private String level;
    private String region;
    private String classKind;
    private CostCondition costCondition;
}
