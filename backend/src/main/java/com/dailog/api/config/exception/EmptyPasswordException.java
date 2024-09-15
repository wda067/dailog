package com.dailog.api.config.exception;

import org.springframework.security.core.AuthenticationException;

public class EmptyPasswordException extends AuthenticationException {

    private static final String MESSAGE = "비밀번호를 입력해 주세요.";

    public EmptyPasswordException() {
        super(MESSAGE);
    }
}