package com.diving.pungdong.dto.account.restore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRestoreInfo {
    @NotEmpty
    private String email;

    @NotEmpty
    private String emailAuthCode;
}
