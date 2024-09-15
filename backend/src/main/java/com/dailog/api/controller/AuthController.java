package com.dailog.api.controller;

import com.dailog.api.service.ReissueService;
import com.dailog.api.service.oAuth2.OAuth2JWTHeaderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final ReissueService reissueService;
    private final OAuth2JWTHeaderService oAuth2JWTHeaderService;

    @PostMapping("/api/auth/reissue")
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        reissueService.response(request, response);
    }

    @PostMapping("/api/oauth2-jwt-header")
    public void oAuth2JWTHeader(HttpServletRequest request, HttpServletResponse response) {
        log.info("oAuth2JWTHeader");
        oAuth2JWTHeaderService.setJWTHeader(request, response);
    }
}
