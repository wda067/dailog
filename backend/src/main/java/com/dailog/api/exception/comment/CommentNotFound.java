package com.dailog.api.exception.comment;

import com.dailog.api.exception.DailogException;

/**
 * status -> 404
 */
public class CommentNotFound extends DailogException {

    private static final String MESSAGE = "존재하지 않는 댓글입니다.";

    public CommentNotFound() {
        super(MESSAGE);
    }

    //public PostNotFound(Throwable cause) {
    //    super(MESSAGE, cause);
    //}


    @Override
    public int getStatusCode() {
        return 404;
    }
}
