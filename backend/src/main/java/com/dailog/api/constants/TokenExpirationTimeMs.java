package com.dailog.api.constants;

public enum TokenExpirationTimeMs {
    ACCESS_TOKEN_EXPIRATION_TIME_MS(60 * 10 * 1000L),  //10분
    REFRESH_TOKEN_EXPIRATION_TIME_MS(60 * 60 * 24 * 1000L);  //24시간

    private final long value;

    TokenExpirationTimeMs(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
