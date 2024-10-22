package com.dailog.api.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.dailog.api.config.filter.CustomLogoutFilter;
import com.dailog.api.config.filter.JWTFilter;
import com.dailog.api.config.filter.JsonEmailPasswordAuthFilter;
import com.dailog.api.config.handler.CustomOAuth2SuccessHandler;
import com.dailog.api.config.handler.Http401Handler;
import com.dailog.api.config.handler.Http403Handler;
import com.dailog.api.config.handler.LoginFailHandler;
import com.dailog.api.config.handler.LoginSuccessHandler;
import com.dailog.api.config.handler.OAuthLoginFailHandler;
import com.dailog.api.domain.Member;
import com.dailog.api.repository.RefreshTokenRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.service.auth.RefreshTokenService;
import com.dailog.api.service.auth.CustomOAuth2UserService;
import com.dailog.api.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers("/assets/**")
                .requestMatchers("/error")
                .requestMatchers("/index.html");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RefreshTokenRepository refreshTokenRepository)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/",
                                "/api/auth/login",
                                "/api/auth/join",
                                "/api/auth/logout",
                                "/api/auth/reissue",
                                "/api/oauth2-jwt-header",
                                "/api/health-check",
                                "/api/stock/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/**/comments/anonymous",
                                "/api/comments/**/delete/anonymous").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/comments/**/anonymous").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    //허용할 Origin
                    configuration.setAllowedOrigins(Arrays.asList(
                            "https://dailog.shop",
                            "http://52.79.177.18",
                            "http://localhost"));
                    //모든 HTTP 메서드 허용
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    //쿠키, 인증 헤더 등을 허용
                    configuration.setAllowCredentials(true);
                    //모든 헤더 허용
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    //캐시된 CORS 응답의 최대 시간
                    configuration.setMaxAge(3600L);  //3600초 (1시간)
                    //클라이언트가 접근할 수 있는 응답 헤더
                    configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "access", "Authorization"));

                    return configuration;
                }))

                .addFilterAt(jsonEmailPasswordAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTFilter(jwtUtil), JsonEmailPasswordAuthFilter.class)
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenService), LogoutFilter.class)

                .exceptionHandling(e -> {
                    e.accessDeniedHandler(new Http403Handler(objectMapper));
                    e.authenticationEntryPoint(new Http401Handler(objectMapper));
                })
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler(new OAuthLoginFailHandler(objectMapper)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(STATELESS))

                .build();
    }

    @Bean
    public JsonEmailPasswordAuthFilter jsonEmailPasswordAuthFilter() {
        JsonEmailPasswordAuthFilter filter = new JsonEmailPasswordAuthFilter("/api/auth/login", objectMapper);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(jwtUtil, refreshTokenService));
        filter.setAuthenticationFailureHandler(new LoginFailHandler(objectMapper));
        filter.setSecurityContextRepository(new NullSecurityContextRepository());
        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService(memberRepository));
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> {
            Member member = memberRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));
            return new CustomUserDetails(member);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
