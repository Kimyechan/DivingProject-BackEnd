package com.diving.pungdong.dto.account.signUp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResult {
    String email;
    String nickName;
}
