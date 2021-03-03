package com.diving.pungdong.dto.lecture.detail;

import com.diving.pungdong.dto.lecture.create.EquipmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LectureDetail {
    private Long id;
    private String title;
    private String classKind;
    private String groupName;
    private String certificateKind;
    private String description;
    private Integer price;
    private Integer period;
    private Integer studentCount;
    private String region;
    private Long instructorId;
    private List<String> lectureUrlList;
    private List<EquipmentDto> equipmentList;
}
