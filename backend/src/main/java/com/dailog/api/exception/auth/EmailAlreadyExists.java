package com.dailog.api.exception.auth;

import com.dailog.api.exception.DailogException;

public class EmailAlreadyExists extends DailogException {

    private static final String MESSAGE = "이미 가입된 이메일입니다.";

    public EmailAlreadyExists() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
