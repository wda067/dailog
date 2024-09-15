package com.dailog.api.exception.comment;

import com.dailog.api.exception.DailogException;

/**
 * status -> 403
 */
public class ForbiddenCommentAccess extends DailogException {

    private static final String MESSAGE = "이 댓글의 작성자가 아닙니다.";

    public ForbiddenCommentAccess() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
