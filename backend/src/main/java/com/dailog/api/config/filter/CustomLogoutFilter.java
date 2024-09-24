package com.dailog.api.config.filter;


import com.dailog.api.service.RefreshTokenService;
import com.dailog.api.util.CookieUtil;
import com.dailog.api.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!requestURI.matches("/api/auth/logout") || !requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        Optional<Cookie> refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refresh"))
                .findFirst();

        if (refreshToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String refresh = refreshToken.get().getValue();
        String username = jwtUtil.getUsername(refresh);
        String category = jwtUtil.getCategory(refresh);

        boolean isNotRefreshToken = !category.equals("refresh");
        if (isNotRefreshToken) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        boolean isRefreshTokenNotFound = refreshTokenService.getTokenByUsername(username)
                .isEmpty();
        if (isRefreshTokenNotFound) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //DB에서 Refresh 토큰 삭제
        refreshTokenService.delete(username);
        //Refresh 토큰의 생명 주기를 0으로 하여 응답
        response.addCookie(CookieUtil.createCookie("refresh", null, 0));
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
