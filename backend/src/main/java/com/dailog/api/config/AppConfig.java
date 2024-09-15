package com.dailog.api.config;

import static java.util.Base64.getDecoder;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
//@Configuration
//@ConfigurationProperties(prefix = "jwt")
public class AppConfig {

    private String jwtKey;
    private byte[] decodedJwtKey;

    public SecretKey getJwtSecretKey() {
        return Keys.hmacShaKeyFor(decodedJwtKey);
    }

    public void setJwtKey(String jwtKey) {
        this.decodedJwtKey = getDecoder().decode(jwtKey);
    }
}
