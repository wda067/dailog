package com.dailog.api.exception.auth;

import com.dailog.api.exception.DailogException;
import lombok.Getter;

/**
 * status -> 400
 */
@Getter
public class SamePassword extends DailogException {

    private static final String MESSAGE = "기존 비밀번호와 새 비밀번호가 같습니다. 새 비밀번호를 다시 작성해 주세요.";

    public SamePassword() {
        super(MESSAGE);
    }

    public SamePassword(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    public int getStatusCode() {
        return 400;
    }
}
