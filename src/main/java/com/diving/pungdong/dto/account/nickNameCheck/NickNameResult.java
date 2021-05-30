package com.diving.pungdong.dto.account.nickNameCheck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NickNameResult {
    private Boolean isExisted;
}
