package com.diving.pungdong.dto.account.signUp;

import com.diving.pungdong.domain.account.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpInfo {
    @NotNull String email;
    @NotNull String password;
    @NotNull String nickName;
    @NotNull String birth;
    @NotNull Gender gender;
}
