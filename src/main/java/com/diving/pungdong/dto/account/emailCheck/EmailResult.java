package com.diving.pungdong.dto.account.emailCheck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EmailResult {
    private boolean existed;
}
