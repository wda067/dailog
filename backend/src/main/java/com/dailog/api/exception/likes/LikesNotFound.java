package com.dailog.api.exception.likes;

import com.dailog.api.exception.DailogException;

/**
 * status -> 404
 */
public class LikesNotFound extends DailogException {

    private static final String MESSAGE = "현재 게시글에 대한 좋아요를 찾을 수 없습니다.";

    public LikesNotFound() {
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
