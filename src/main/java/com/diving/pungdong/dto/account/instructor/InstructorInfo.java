package com.diving.pungdong.dto.account.instructor;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorInfo {
    @NotNull
    private Organization organization;

    @NotEmpty
    private String selfIntroduction;
}
