package com.diving.pungdong.dto.location.delete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDeleteInfo {
    private Long lectureId;
    private List<Long> lectureImageIds;
}
