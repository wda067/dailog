package com.dailog.api.exception.member;

import com.dailog.api.exception.DailogException;

/**
 * status -> 404
 */
public class MemberNotFound extends DailogException {

    private static final String MESSAGE = "존재하지 않는 회원입니다.";

    public MemberNotFound() {
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
