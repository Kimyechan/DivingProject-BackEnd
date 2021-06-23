package com.diving.pungdong.service.account.read;

import com.diving.pungdong.domain.account.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountBasicInfo {
    private Long id;

    private String email;

    private String nickName;

    private String birth;

    private Gender gender;

    private String phoneNumber;
}
