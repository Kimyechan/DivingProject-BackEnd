package com.diving.pungdong.dto.lecture.update;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureUpdateInfo {
    @NotNull
    private Long id;

    @NotEmpty
    private String title;

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

    @NotEmpty
    private String region;

    @NotNull
    private Integer maxNumber;

    @NotNull
    private Integer period;

    @NotNull
    private LocalTime lectureTime;

    private Set<String> serviceTags = new HashSet<>();
}
