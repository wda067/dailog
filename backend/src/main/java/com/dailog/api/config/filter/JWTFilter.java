package com.dailog.api.config.filter;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import com.dailog.api.config.CustomUserDetails;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.request.member.MemberAuthRequest;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.request.oAuth2.MemberOAuth2Request;
import com.dailog.api.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter chain 요청에 담긴 JWT를 검증하기위한 커스텀 필터
 * 요청 쿠키에 JWT가 존재하는 경우 JWT를 검증하고 강제로 세션을 생성
 * 이 세션은 STATELESS 상태로 관리되기 때문에 요청이 끝나면 소멸
 */
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, IOException {

        String access = request.getHeader("Authorization");

        if (access == null) {
            filterChain.doFilter(request, response);
            return;
        }

        access = access.substring("Bearer".length()).trim();

        //token 만료 여부 확인 -> 만료되면 401 응답
        try {
            jwtUtil.isExpired(access);
        } catch (ExpiredJwtException e) {
            handleException(response, "Access token expired", SC_UNAUTHORIZED);
            return;
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            //JWT 파싱 오류 처리
            handleException(response, "Invalid JWT token", SC_UNAUTHORIZED);
            return;
        }

        boolean isNotAccessToken = !jwtUtil.getCategory(access).equals("access");
        if (isNotAccessToken) {
            handleException(response, "Invalid access token", SC_UNAUTHORIZED);
            return;
        }

        String username = jwtUtil.getUsername(access);
        String roleString = jwtUtil.getRole(access);
        Role role = Role.valueOf(roleString);
        Boolean oAuth2Login = jwtUtil.isOAuth2Login(access);

        Authentication authToken;
        if (oAuth2Login) {
            String provider = jwtUtil.getProvider(access);

            MemberOAuth2Request oAuth2Request = MemberOAuth2Request.builder()
                    .username(username)
                    .role(role)
                    .provider(provider)
                    .build();

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2Request);

            authToken = new OAuth2AuthenticationToken(
                    customOAuth2User,
                    customOAuth2User.getAuthorities(),
                    provider);
        } else {
            MemberAuthRequest memberAuthRequest = MemberAuthRequest.builder()
                    .username(username)
                    .role(role)
                    .build();

            Member member = Member.builder()
                    .email(memberAuthRequest.getUsername())
                    .role(memberAuthRequest.getRole())
                    .build();
            CustomUserDetails customUserDetails = new CustomUserDetails(member);

            authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities());
        }

        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("JWTFilter authenticated");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication: {}", authentication);
        log.info("authentication.isAuthenticated(): {}", authentication.isAuthenticated());
        log.info("authentication.getAuthorities(): {}", authentication.getAuthorities());
        log.info("authentication.getPrincipal(): {}", authentication.getPrincipal());
        log.info("authentication.getCredentials(): {}", authentication.getCredentials());
        log.info("authentication.getDetails(): {}", authentication.getDetails());
        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        PrintWriter writer = response.getWriter();
        writer.println(message);
        writer.flush();
    }
}