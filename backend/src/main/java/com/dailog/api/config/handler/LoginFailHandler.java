package com.dailog.api.config.handler;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.dailog.api.config.exception.EmptyEmailException;
import com.dailog.api.config.exception.EmptyPasswordException;
import com.dailog.api.config.exception.InvalidEmailException;
import com.dailog.api.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
@RequiredArgsConstructor
public class LoginFailHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String message;
        if (exception instanceof EmptyEmailException
                || exception instanceof InvalidEmailException
                || exception instanceof EmptyPasswordException) {
            message = exception.getMessage();
        } else {
            message = "이메일 혹은 비밀번호가 올바르지 않습니다.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("400")
                .message(message)
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.displayName());
        response.setStatus(SC_BAD_REQUEST);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
