package com.dailog.api.exception.member;

import com.dailog.api.exception.DailogException;
import lombok.Getter;

/**
 * status -> 400
 */
@Getter
public class InvalidPasswordConfirm extends DailogException {

    private static final String MESSAGE = "새로운 비밀번호가 일치하지 않습니다.";

    public InvalidPasswordConfirm() {
        super(MESSAGE);
    }

    public InvalidPasswordConfirm(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    public int getStatusCode() {
        return 400;
    }
}
