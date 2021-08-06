package com.diving.pungdong.dto.account.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordInfo {
    @Email
    private String email;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String authCode;
}
