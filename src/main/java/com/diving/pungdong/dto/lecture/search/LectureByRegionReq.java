package com.diving.pungdong.dto.lecture.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LectureByRegionReq {
    @NotEmpty
    private String region;
}
