package com.dailog.api.exception.likes;

import com.dailog.api.exception.DailogException;

public class AlreadyLikedPost extends DailogException {

    private static final String MESSAGE = "이미 좋아요를 누른 게시글입니다.";

    public AlreadyLikedPost() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
