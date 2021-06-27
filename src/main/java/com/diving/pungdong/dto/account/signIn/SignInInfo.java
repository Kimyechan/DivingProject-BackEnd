package com.diving.pungdong.dto.account.signIn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInInfo {
    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
