package com.diving.pungdong.service.kafka.dto.account;

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
