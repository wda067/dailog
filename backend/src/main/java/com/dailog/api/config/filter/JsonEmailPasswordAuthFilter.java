package com.dailog.api.config.filter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

import com.dailog.api.config.exception.EmptyEmailException;
import com.dailog.api.config.exception.EmptyPasswordException;
import com.dailog.api.config.exception.InvalidEmailException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

@Slf4j
public class JsonEmailPasswordAuthFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;

    public JsonEmailPasswordAuthFilter(String loginUrl, ObjectMapper objectMapper) {
        super(loginUrl);
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (request.getContentType() == null || !request.getContentType().equals(APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }
        //JSON 형식의 데이터를 Java 객체로 변환
        EmailPassword emailPassword = objectMapper.readValue(request.getInputStream(), EmailPassword.class);

        if (emailPassword.getEmail() == null || emailPassword.getEmail().isEmpty()) {
            throw new EmptyEmailException();
        }

        if (!emailPassword.getEmail()
                .matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new InvalidEmailException();
        }

        if (emailPassword.getPassword() == null || emailPassword.getPassword().isEmpty()) {
            throw new EmptyPasswordException();
        }

        UsernamePasswordAuthenticationToken token = unauthenticated(
                emailPassword.getEmail(),
                emailPassword.getPassword()
        );
        //토큰 객체에 요청의 세부 정보를 추가하여 이후 인증 절차에 사용할 수 있도록 함
        token.setDetails(this.authenticationDetailsSource.buildDetails(request));

        //실제로 인증을 처리
        return this.getAuthenticationManager().authenticate(token);
    }

    @Getter
    private static class EmailPassword {

        private String email;
        private String password;
    }
}
