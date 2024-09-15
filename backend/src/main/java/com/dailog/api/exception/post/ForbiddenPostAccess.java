package com.dailog.api.exception.post;

import com.dailog.api.exception.DailogException;

/**
 * status -> 403
 */
public class ForbiddenPostAccess extends DailogException {

    private static final String MESSAGE = "이 게시글의 작성자가 아닙니다.";

    public ForbiddenPostAccess() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
