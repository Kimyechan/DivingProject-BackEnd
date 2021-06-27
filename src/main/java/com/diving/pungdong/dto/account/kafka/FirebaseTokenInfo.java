package com.diving.pungdong.dto.account.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class FirebaseTokenInfo {
    private String id;
    private String token;
}
