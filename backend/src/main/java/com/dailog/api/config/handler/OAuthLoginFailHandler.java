package com.dailog.api.config.handler;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.dailog.api.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
@RequiredArgsConstructor
public class OAuthLoginFailHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String message;
        int status;

        if (exception instanceof OAuth2AuthenticationException) {
            message = "OAuth2 인증에 실패하였습니다.";
            status = SC_UNAUTHORIZED;
        } else {
            message = "인증에 실패하였습니다.";
            status = SC_BAD_REQUEST;
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(String.valueOf(status))
                .message(message)
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.displayName());
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), errorResponse);

        log.error("Authentication failure: {}", exception.getMessage());
    }
}
