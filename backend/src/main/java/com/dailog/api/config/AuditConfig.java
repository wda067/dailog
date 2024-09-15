package com.dailog.api.config;

import com.dailog.api.request.oAuth2.CustomOAuth2User;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = "";

                if (auth instanceof UsernamePasswordAuthenticationToken) {
                    userId = auth.getName();
                } else if (auth instanceof OAuth2AuthenticationToken) {
                    CustomOAuth2User principal = (CustomOAuth2User) auth.getPrincipal();
                    userId = principal.getUsername();
                }
                return Optional.of(userId);
            }
        };
    }
}
