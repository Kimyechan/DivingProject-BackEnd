package com.diving.pungdong.dto.lecture.create;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLectureReq {
    @NotEmpty
    private String title;
    @NotEmpty private String classKind;
    @NotEmpty private Organization organization;
    @NotEmpty private String level;
    @NotEmpty private String description;
    @NotEmpty private Integer price;
    @NotEmpty private String region;
    private List<EquipmentDto> equipmentList = new ArrayList<>();
}
