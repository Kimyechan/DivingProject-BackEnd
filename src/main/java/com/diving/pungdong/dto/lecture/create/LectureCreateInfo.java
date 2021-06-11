package com.diving.pungdong.dto.lecture.create;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCreateInfo {
    @NotEmpty
    private String title;

    @NotEmpty
    private String region;

    @NotEmpty
    private String classKind;

    @NotNull
    private Organization organization;

    @NotEmpty
    private String level;

    @NotEmpty
    private String description;

    @NotNull
    private Integer price;

    @NotNull
    private Integer period;

    @NotNull
    private LocalTime lectureTime;
}
