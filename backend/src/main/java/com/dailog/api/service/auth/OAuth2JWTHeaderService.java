package com.dailog.api.service.auth;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

import com.dailog.api.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OAuth2JWTHeaderService {

    public void setJWTHeader(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        //요청 쿠키에서 Access 토큰 추출
        Optional<Cookie> access = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("access"))
                .findFirst();

        //Access 토큰이 존재하지 않을 경우 400 응답
        if (access.isEmpty()) {
            response.setStatus(SC_BAD_REQUEST);
            return;
        }

        String accessToken = access.get().getValue();
        //쿠키의 Access 토큰은 만료시킴
        response.addCookie(CookieUtil.createCookie("access", null, 0));
        response.setHeader("Access-Control-Expose-Headers", "access");
        response.addHeader("access", accessToken);
        //response.addHeader("Authorization", "Bearer " + accessToken);
        response.setStatus(SC_OK);
    }
}
