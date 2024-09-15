package com.dailog.api.exception.member;

import com.dailog.api.exception.DailogException;

public class InvalidNickname extends DailogException {

    private static final String MESSAGE = "이미 존재하는 별명입니다.";

    public InvalidNickname() {
        super(MESSAGE);
    }

    public InvalidNickname(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
