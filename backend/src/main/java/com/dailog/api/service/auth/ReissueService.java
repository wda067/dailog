package com.dailog.api.service.auth;

import static com.dailog.api.constants.TokenExpirationTimeS.REFRESH_TOKEN_EXPIRATION_TIME_S;
import static com.dailog.api.constants.TokenExpirationTimeMs.ACCESS_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeMs.REFRESH_TOKEN_EXPIRATION_TIME_MS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.dailog.api.util.CookieUtil;
import com.dailog.api.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public ResponseEntity<?> response(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(BAD_REQUEST).body("Cookie not found");
        }

        Optional<Cookie> optionalRefresh = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refresh"))
                .findFirst();

        //쿠키에 refresh 토큰이 존재하지 않음
        if (optionalRefresh.isEmpty()) {
            return ResponseEntity.status(BAD_REQUEST).body("Refresh token not found");
        }

        String refreshToken = optionalRefresh.get().getValue();

        //만료된 토큰은 payload를 읽을 수 없음
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(BAD_REQUEST).body("Refresh token expired");
        }

        //refresh 토큰이 아님
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid refresh token");
        }

        String username = jwtUtil.getUsername(refreshToken);
        if (username == null) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid refresh token");
        }

        //DB에 존재하지 않는 (혹은 블랙리스트 처리된) refresh 토큰
        boolean isRefreshTokenNotFound = refreshTokenService.getTokenByUsername(username)
                .isEmpty();
        if (isRefreshTokenNotFound) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid refresh token");
        }

        //토큰 재발급
        String roleString = jwtUtil.getRole(refreshToken);
        Boolean oAuth2Login = jwtUtil.isOAuth2Login(refreshToken);
        String provider = jwtUtil.getProvider(refreshToken);

        String newAccess = jwtUtil.createJwt("access", username, roleString,
                ACCESS_TOKEN_EXPIRATION_TIME_MS.getValue(), oAuth2Login, provider);
        String newRefresh = jwtUtil.createJwt("refresh", username, roleString,
                REFRESH_TOKEN_EXPIRATION_TIME_MS.getValue(), oAuth2Login, provider);

        //기존 refresh DB 삭제, 새로운 refresh 저장
        refreshTokenService.delete(username);
        refreshTokenService.save(username, newRefresh, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue());

        response.setHeader("access", newAccess);
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue()));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
