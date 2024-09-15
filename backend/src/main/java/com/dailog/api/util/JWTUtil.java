package com.dailog.api.util;

import static java.util.Base64.getDecoder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(String jwtKey) {
        this.secretKey = Keys.hmacShaKeyFor(getDecoder().decode(jwtKey));
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    //public Collection<? extends GrantedAuthority> getRole(String token) {
    //    List<?> rawAuthorities = Jwts.parser().verifyWith(secretKey).build()
    //            .parseSignedClaims(token)
    //            .getPayload()
    //            .get("authorities", List.class);
    //
    //    List<String> authorities = rawAuthorities.stream()
    //            .filter(String.class::isInstance)  //String 타입만 필터링
    //            .map(Object::toString)
    //            .toList();
    //
    //    return authorities.stream()
    //            .map(SimpleGrantedAuthority::new)
    //            .collect(Collectors.toList());
    //}

    //public String getRole(String token) {
    //    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
    //            .get("role", String.class);
    //}

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("category", String.class);
    }

    public Boolean isOAuth2Login(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("oAuth2Login", Boolean.class);
    }

    public String getProvider(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("provider", String.class);
    }

    public String createJwt(String category, String username, String role,
                            Long expiredMs, Boolean oauth2Login, String provider) {

        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .claim("oAuth2Login", oauth2Login)
                .claim("provider", provider)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    //HMAC 알고리즘으로 임의의 비밀키 생성
    public String createEncodedKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
