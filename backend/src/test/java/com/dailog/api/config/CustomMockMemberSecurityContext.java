package com.dailog.api.config;

import com.dailog.api.domain.Member;
import com.dailog.api.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class CustomMockMemberSecurityContext implements WithSecurityContextFactory<CustomMockMember> {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SecurityContext createSecurityContext(CustomMockMember annotation) {
        String encryptedPassword = passwordEncoder.encode(annotation.password());
        Member member = Member.builder()
                .email(annotation.email())
                .password(encryptedPassword)
                .name(annotation.name())
                .nickname(annotation.nickname())
                .role(annotation.role())
                .build();

        memberRepository.save(member);

        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                member.getPassword(),
                member.getRole().getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);

        return context;
    }
}
