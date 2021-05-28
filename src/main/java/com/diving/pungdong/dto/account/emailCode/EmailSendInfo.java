package com.diving.pungdong.dto.account.emailCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendInfo {
    @Email
    private String email;
}
