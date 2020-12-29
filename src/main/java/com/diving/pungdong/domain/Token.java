package com.diving.pungdong.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("tokens")
public class Token {
    @Id
    private Long id;

    private String accessToken;

    private String refreshToken;
}
