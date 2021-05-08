package com.diving.pungdong.dto.account;

import com.diving.pungdong.domain.account.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfo {
    private String id;
    private String password;
    private Set<Role> roles;
}
