package com.diving.pungdong.dto.account.update;

import com.diving.pungdong.domain.account.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountUpdateInfo {
    @NotEmpty private String birth;
    @NotNull private Gender gender;
    @NotEmpty private String phoneNumber;
}
