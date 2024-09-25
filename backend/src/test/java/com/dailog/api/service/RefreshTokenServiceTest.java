package com.dailog.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dailog.api.domain.RefreshToken;
import com.dailog.api.exception.InvalidRequest;
import com.dailog.api.repository.RefreshTokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("Refresh 토큰 저장")
    void should_SaveToken_When_ValidToken() {
        //given
        String username = "testUser";
        String refreshToken = "testRefreshToken";
        long expiration = 3600L;

        //when
        refreshTokenService.save(username, refreshToken, expiration);

        //then
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(username);
        assertTrue(optionalRefreshToken.isPresent());
        RefreshToken findRefreshToken = optionalRefreshToken.get();

        assertEquals(username, findRefreshToken.getUsername());
        assertEquals(refreshToken, findRefreshToken.getRefreshToken());
        assertEquals(expiration, findRefreshToken.getExpiration());
    }

    @Test
    @DisplayName("Refresh 토큰 삭제")
    void should_DeleteToken_When_ValidUsername() {
        //given
        RefreshToken refreshToken = RefreshToken.builder()
                .username("testUser")
                .refreshToken("testRefreshToken")
                .expiration(3600L)
                .build();
        refreshTokenRepository.save(refreshToken);

        //when
        refreshTokenService.delete(refreshToken.getUsername());

        //then
        assertEquals(0, refreshTokenRepository.count());
    }

    @Test
    @DisplayName("Refresh 토큰 삭제 실패 - 유효하지 않은 Username")
    void should_FailToDeleteToken_When_InvalidUsername() {
        //given
        RefreshToken refreshToken = RefreshToken.builder()
                .username("testUser")
                .refreshToken("testRefreshToken")
                .expiration(3600L)
                .build();
        refreshTokenRepository.save(refreshToken);

        //expected
        assertThrows(InvalidRequest.class, () -> refreshTokenService.delete("InvalidUsername"));
    }

    @Test
    @DisplayName("Refresh 토큰 조회")
    void should_GetToken_When_ValidUsername() {
        //given
        RefreshToken refreshToken = RefreshToken.builder()
                .username("testUser")
                .refreshToken("testRefreshToken")
                .expiration(3600L)
                .build();
        refreshTokenRepository.save(refreshToken);

        //expected
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.getTokenByUsername(
                refreshToken.getUsername());
        assertTrue(optionalRefreshToken.isPresent());
    }

    @Test
    @DisplayName("Refresh 토큰 조회 실패 - 유효하지 않은 Username")
    void should_FailToGetToken_When_InvalidUsername() {
        //given
        RefreshToken refreshToken = RefreshToken.builder()
                .username("testUser")
                .refreshToken("testRefreshToken")
                .expiration(3600L)
                .build();
        refreshTokenRepository.save(refreshToken);

        //expected
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.getTokenByUsername("InvalidUsername");
        assertTrue(optionalRefreshToken.isEmpty());
    }
}