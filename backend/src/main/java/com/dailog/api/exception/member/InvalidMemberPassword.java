package com.dailog.api.exception.member;

import com.dailog.api.exception.DailogException;

public class InvalidMemberPassword extends DailogException {

    private static final String MESSAGE = "현재 비밀번호가 올바르지 않습니다.";

    public InvalidMemberPassword() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
