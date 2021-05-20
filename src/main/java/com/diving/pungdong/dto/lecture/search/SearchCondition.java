package com.diving.pungdong.dto.lecture.search;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCondition {
    private Organization organization;
    private String level;
    private String region;
    private CostCondition costCondition;
}
