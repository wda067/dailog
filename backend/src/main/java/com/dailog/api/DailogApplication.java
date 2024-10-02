package com.dailog.api;

import com.dailog.api.util.JWTUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import java.security.NoSuchAlgorithmException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(JWTUtil.class)
@EnableEncryptableProperties
@EnableScheduling
@SpringBootApplication
public class DailogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailogApplication.class, args);
    }
}