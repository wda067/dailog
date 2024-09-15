package com.dailog.api.exception.auth;

import com.dailog.api.exception.DailogException;

public class InvalidSignInInformation extends DailogException {

    private static final String MESSAGE = "이메일 또는 비밀번호가 잘못 되었습니다.";

    public InvalidSignInInformation() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
