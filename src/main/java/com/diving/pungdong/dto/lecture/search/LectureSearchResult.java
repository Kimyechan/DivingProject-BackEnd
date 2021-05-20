package com.diving.pungdong.dto.lecture.search;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureSearchResult {
    private Long id;
    private String title;
    private String classKind;
    private Organization organization;
    private String level;
    private Integer price;
    private String region;
    private List<String> imageURL = new ArrayList<>();
}
