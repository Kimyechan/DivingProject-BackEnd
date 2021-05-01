package com.diving.pungdong.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthToken {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private Integer expires_in;
    private String scope;
    private String jti;
}
