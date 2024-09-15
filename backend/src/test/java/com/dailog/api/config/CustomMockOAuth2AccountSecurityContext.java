package com.dailog.api.config;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.request.oAuth2.MemberOAuth2Request;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class CustomMockOAuth2AccountSecurityContext implements WithSecurityContextFactory<CustomMockOAuth2Account> {

    private final MemberRepository memberRepository;

    @Override
    public SecurityContext createSecurityContext(CustomMockOAuth2Account annotation) {
        Member member = Member.builder()
                .email(annotation.username())
                .name(annotation.name())
                .nickname(annotation.nickname())
                .role(annotation.role())
                .build();
        memberRepository.save(member);

        MemberOAuth2Request oAuth2Request = MemberOAuth2Request.builder()
                .username(annotation.username())
                .name(annotation.name())
                .nickname(annotation.nickname())
                .role(annotation.role())
                .provider(annotation.provider())
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2Request);

        Authentication authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                annotation.provider()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);

        return context;
    }
}
