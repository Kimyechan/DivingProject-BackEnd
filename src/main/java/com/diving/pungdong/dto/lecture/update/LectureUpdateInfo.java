package com.diving.pungdong.dto.lecture.update;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureUpdateInfo {
    private Long id;
    private String title;
    private String classKind;
    private Organization organization;
    private String level;
    private String description;
    private Integer price;
    private Integer period;
    private Integer studentCount;
    private String region;
    private List<LectureImageUpdate> lectureImageUpdateList;
    private List<EquipmentUpdate> equipmentUpdateList;
}
