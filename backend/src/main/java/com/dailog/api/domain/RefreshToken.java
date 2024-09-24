package com.dailog.api.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refresh")
public class RefreshToken {

    @Id
    private String username;

    private String refreshToken;

    @TimeToLive
    private long expiration;

    @Builder
    public RefreshToken(String username, String refreshToken, long expiration) {
        this.username = username;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
