package com.dailog.api.exception.post;

import com.dailog.api.exception.DailogException;

/**
 * status -> 404
 */
public class PostNotFound extends DailogException {

    private static final String MESSAGE = "존재하지 않는 글입니다.";

    public PostNotFound() {
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
