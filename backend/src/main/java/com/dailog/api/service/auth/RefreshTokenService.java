package com.dailog.api.service.auth;

import com.dailog.api.domain.RefreshToken;
import com.dailog.api.exception.InvalidRequest;
import com.dailog.api.repository.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(String username, String refreshToken, long expiration) {
        RefreshToken token = RefreshToken.builder()
                .username(username)
                .refreshToken(refreshToken)
                .expiration(expiration)
                .build();

        refreshTokenRepository.save(token);
    }

    public void delete(String username) {
        RefreshToken refreshToken = refreshTokenRepository.findById(username)
                .orElseThrow(InvalidRequest::new);
        refreshTokenRepository.delete(refreshToken);
    }

    public Optional<RefreshToken> getTokenByUsername(String username) {
        return refreshTokenRepository.findById(username);
    }
}
