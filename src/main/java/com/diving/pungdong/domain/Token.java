package com.diving.pungdong.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("tokens")
public class Token {
    @Id
    private Long id;

    private String accessToken;

    private String refreshToken;
}
