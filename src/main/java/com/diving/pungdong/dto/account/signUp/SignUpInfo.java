package com.diving.pungdong.dto.account.signUp;

import com.diving.pungdong.domain.account.Gender;
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
public class SignUpInfo {
    @NotEmpty String verifyCode;
    @NotEmpty String email;
    @NotEmpty String password;
    @NotEmpty String nickName;
    @NotEmpty String birth;
    @NotEmpty String phoneNumber;
    @NotNull Gender gender;
}
