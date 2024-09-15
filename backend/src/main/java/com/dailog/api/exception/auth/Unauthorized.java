package com.dailog.api.exception.auth;

import com.dailog.api.exception.DailogException;

/**
 * status -> 401
 */
public class Unauthorized extends DailogException {

    private static final String MESSAGE = "로그인 해주세요.";

    public Unauthorized() {
        super(MESSAGE);
    }

    public Unauthorized(String message) {
        super(message);
    }

    //public Unauthorized(Throwable cause) {
    //    super(MESSAGE, cause);
    //}


    @Override
    public int getStatusCode() {
        return 401;
    }
}
