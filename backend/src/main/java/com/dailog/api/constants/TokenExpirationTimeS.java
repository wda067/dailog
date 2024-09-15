package com.dailog.api.constants;

public enum TokenExpirationTimeS {
    ACCESS_TOKEN_EXPIRATION_TIME_S(60 * 10),  //10분
    REFRESH_TOKEN_EXPIRATION_TIME_S(60 * 60 * 24);  //24시간

    private final int value;

    TokenExpirationTimeS(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
