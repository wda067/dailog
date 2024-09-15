package com.dailog.api.config.exception;

import org.springframework.security.core.AuthenticationException;

public class EmptyEmailException extends AuthenticationException {

    private static final String MESSAGE = "이메일을 입력해 주세요.";

    public EmptyEmailException() {
        super(MESSAGE);
    }
}