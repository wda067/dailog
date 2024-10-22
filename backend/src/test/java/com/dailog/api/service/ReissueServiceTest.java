package com.dailog.api.service;

import static com.dailog.api.constants.TokenExpirationTimeMs.ACCESS_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeMs.REFRESH_TOKEN_EXPIRATION_TIME_MS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dailog.api.domain.RefreshToken;
import com.dailog.api.repository.RefreshTokenRepository;
import com.dailog.api.service.auth.RefreshTokenService;
import com.dailog.api.service.auth.ReissueService;
import com.dailog.api.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
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
    private RefreshTokenService refreshTokenService;

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
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("쿠키에 refresh token이 존재할 경우 access token과 함께 재발급하여 응답한다.")
    public void testResponse_Success() {
        // Given
        Cookie cookie = new Cookie("refresh", "validRefreshToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // Ensure all methods return expected values
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("refresh");
        when(jwtUtil.getUsername(anyString())).thenReturn("testUser");
        when(jwtUtil.getRole(anyString())).thenReturn("userRole");
        when(jwtUtil.isOAuth2Login(anyString())).thenReturn(false);
        when(jwtUtil.getProvider(anyString())).thenReturn("provider");

        RefreshToken refreshToken = RefreshToken.builder()
                .username("testUser")
                .refreshToken("validRefreshToken")
                .expiration(REFRESH_TOKEN_EXPIRATION_TIME_MS.getValue())
                .build();

        when(refreshTokenService.getTokenByUsername(anyString())).thenReturn(Optional.of(refreshToken));

        //Use eq() to wrap raw string values to match them with argument matchers
        when(jwtUtil.createJwt(eq("access"), eq("testUser"), eq("userRole"),
                eq(ACCESS_TOKEN_EXPIRATION_TIME_MS.getValue()), eq(false), eq("provider")))
                .thenReturn("newAccessToken");

        when(jwtUtil.createJwt(eq("refresh"), eq("testUser"), eq("userRole"),
                eq(REFRESH_TOKEN_EXPIRATION_TIME_MS.getValue()), eq(false), eq("provider")))
                .thenReturn("newRefreshToken");

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.OK, result.getStatusCode());

        // Verify that setHeader is called with the correct token
        verify(response).setHeader("access", "newAccessToken");
        verify(response).addCookie(any(Cookie.class));
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
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

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
        when(refreshTokenService.getTokenByUsername(jwtUtil.getUsername(validToken))).thenReturn(Optional.empty());

        //when
        ResponseEntity<?> result = reissueService.response(request, response);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid refresh token", result.getBody());
    }
}