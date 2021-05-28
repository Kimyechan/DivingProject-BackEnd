package com.diving.pungdong.dto.account.emailCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAuthInfo {
    private String email;
    private String code;
}
