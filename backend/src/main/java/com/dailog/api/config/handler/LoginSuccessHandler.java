package com.dailog.api.config.handler;

import static com.dailog.api.constants.TokenExpirationTimeMs.ACCESS_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeMs.REFRESH_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeS.REFRESH_TOKEN_EXPIRATION_TIME_S;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

import com.dailog.api.domain.enums.Role;
import com.dailog.api.service.auth.RefreshTokenService;
import com.dailog.api.util.CookieUtil;
import com.dailog.api.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Role role = Role.findByAuthority(authorities);
        String roleString = role.name();

        String access = jwtUtil.createJwt("access", username, roleString,
                ACCESS_TOKEN_EXPIRATION_TIME_MS.getValue(), false, null);
        response.setHeader("access", access);

        String refresh = jwtUtil.createJwt("refresh", username, roleString,
                REFRESH_TOKEN_EXPIRATION_TIME_MS.getValue(), false, null);
        response.addCookie(CookieUtil.createCookie("refresh", refresh, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue()));

        refreshTokenService.save(username, refresh, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue());

        response.setStatus(SC_OK);
    }
}
