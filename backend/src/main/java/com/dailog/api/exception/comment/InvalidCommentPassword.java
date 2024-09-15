package com.dailog.api.exception.comment;

import com.dailog.api.exception.DailogException;

public class InvalidCommentPassword extends DailogException {

    private static final String MESSAGE = "댓글 비밀번호가 잘못 되었습니다.";

    public InvalidCommentPassword() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
