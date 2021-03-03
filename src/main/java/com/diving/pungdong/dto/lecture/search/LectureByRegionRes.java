package com.diving.pungdong.dto.lecture.search;

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
public class LectureByRegionRes {
    private Long id;
    private String title;
    private String classKind;
    private String groupName;
    private String certificateKind;
    private Integer price;
    private String region;
    private List<String> imageURL = new ArrayList<>();
}
