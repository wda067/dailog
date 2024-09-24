package com.dailog.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dailog.api.repository.RefreshTokenRepository;
import com.dailog.api.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ReissueServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ReissueService reissueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("쿠키가 존재하지 않는다.")
    void should_ReturnBadRequest_When_CookieIsEmpty() {
        //given
        when(request.getCookies()).thenReturn(null);

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Cookie not found", result.getBody());
    }

    @Test
    @DisplayName("refresh token이 존재하지 않는다.")
    void should_ReturnBadRequest_When_RefreshTokenIsEmpty() {
        //given
        Cookie cookie = new Cookie("notRefresh", "");
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Refresh token not found", result.getBody());
    }

    @Test
    @DisplayName("refresh token이 만료되었다.")
    void should_ReturnBadRequest_When_RefreshTokenIsExpired() {
        //given
        String expiredToken = "expiredToken";
        Cookie refreshCookie = new Cookie("refresh", expiredToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        doThrow(ExpiredJwtException.class).when(jwtUtil).isExpired(expiredToken);

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Refresh token expired", result.getBody());
    }

    @Test
    @DisplayName("쿠키의 토큰이 refresh token이 아니다.")
    public void should_ReturnBadRequest_When_TokenIsNotRefreshToken() {
        //given
        String invalidToken = "invalidToken";
        Cookie refreshCookie = new Cookie("refresh", invalidToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtil.getCategory(invalidToken)).thenReturn("access");

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid refresh token", result.getBody());
    }

    @Test
    @DisplayName("refresh token이 DB에 존재하지 않는다.")
    public void should_BadRequest_When_RefreshTokenIsNotInDB() {
        //given
        String validToken = "validToken";
        Cookie refreshCookie = new Cookie("refresh", validToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtil.getCategory(validToken)).thenReturn("refresh");
        when(refreshTokenRepository.existsByRefreshToken(validToken)).thenReturn(false);

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid refresh token", result.getBody());
    }

    @Test
    @DisplayName("쿠키에 refresh token이 존재할 경우 access token과 함께 재발급하여 응답한다.")
    public void testResponse_Success() {
        //given
        String refreshToken = "validToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        String username = "testUser";
        String role = "ROLE_MEMBER";
        Cookie refreshCookie = new Cookie("refresh", refreshToken);

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtil.getCategory(refreshToken)).thenReturn("refresh");
        when(refreshTokenRepository.existsByRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.getRole(refreshToken)).thenReturn(role);
        when(jwtUtil.createJwt("access", username, role, 60 * 10 * 1000L, false, null)).thenReturn(newAccessToken);
        when(jwtUtil.createJwt("refresh", username, role, 60 * 60 * 24 * 1000L, false, null)).thenReturn(newRefreshToken);

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(response).setHeader("access", newAccessToken);
        verify(response).addCookie(any(Cookie.class));
    }
}