package com.dailog.api.config.handler;

import static com.dailog.api.constants.TokenExpirationTimeMs.ACCESS_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeMs.REFRESH_TOKEN_EXPIRATION_TIME_MS;
import static com.dailog.api.constants.TokenExpirationTimeS.ACCESS_TOKEN_EXPIRATION_TIME_S;
import static com.dailog.api.constants.TokenExpirationTimeS.REFRESH_TOKEN_EXPIRATION_TIME_S;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.dailog.api.domain.enums.Role;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.service.ReissueService;
import com.dailog.api.util.CookieUtil;
import com.dailog.api.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;

    @Value("${redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException, IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String name = customOAuth2User.getName();
        String username = customOAuth2User.getUsername();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Role role = Role.findByAuthority(authorities);
        String roleString = role.name();

        String nickname = customOAuth2User.getNickname();
        String provider = customOAuth2User.getProvider();

        String access = jwtUtil.createJwt("access", username, roleString,
                ACCESS_TOKEN_EXPIRATION_TIME_MS.getValue(), true, provider);
        String refresh = jwtUtil.createJwt("refresh", username, roleString,
                REFRESH_TOKEN_EXPIRATION_TIME_MS.getValue(), true, provider);

        reissueService.saveRefreshToken(username, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue(), refresh);

        response.addCookie(CookieUtil.createCookie("access", access, ACCESS_TOKEN_EXPIRATION_TIME_S.getValue()));
        response.addCookie(CookieUtil.createCookie("refresh", refresh, REFRESH_TOKEN_EXPIRATION_TIME_S.getValue()));

        String encodedName = URLEncoder.encode(name, UTF_8);
        String encodedNickname = URLEncoder.encode(nickname, UTF_8);
        String encodedRole = URLEncoder.encode(roleString, UTF_8);
        String finalRedirectUrl = String.format("%s?name=%s&nickname=%s&role=%s",
                redirectUrl, encodedName, encodedNickname, encodedRole);

        response.sendRedirect(finalRedirectUrl);
    }
}