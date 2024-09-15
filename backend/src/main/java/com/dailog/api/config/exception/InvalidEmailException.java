package com.dailog.api.config.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidEmailException extends AuthenticationException {

    private static final String MESSAGE = "이메일 형식으로 입력해 주세요.";

    public InvalidEmailException() {
        super(MESSAGE);
    }
}