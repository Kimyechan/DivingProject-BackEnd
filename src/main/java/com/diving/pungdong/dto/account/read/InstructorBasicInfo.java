package com.diving.pungdong.dto.account.read;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InstructorBasicInfo {
    private Long id;

    private Organization organization;

    private String selfIntroduction;
}
