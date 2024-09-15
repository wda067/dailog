package com.dailog.api.exception.comment;

import com.dailog.api.exception.DailogException;
import lombok.Getter;

@Getter
public class InvalidComment extends DailogException {

    private static final String MESSAGE = "잘못된 댓글입니다.";

    public InvalidComment() {
        super(MESSAGE);
    }

    public InvalidComment(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    public int getStatusCode() {
        return 400;
    }
}
