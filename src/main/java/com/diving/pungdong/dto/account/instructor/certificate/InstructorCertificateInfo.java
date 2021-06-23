package com.diving.pungdong.dto.account.instructor.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InstructorCertificateInfo {
    private Long id;
    private String imageUrl;
}
