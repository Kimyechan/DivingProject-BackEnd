package com.diving.pungdong.dto.account.instructor;


import com.diving.pungdong.domain.lecture.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorRequestInfo {
    private String email;
    private String nickName;
    private String phoneNumber;
    private Organization organization;
    private String selfIntroduction;
    private List<String> certificateImageUrls;
}
