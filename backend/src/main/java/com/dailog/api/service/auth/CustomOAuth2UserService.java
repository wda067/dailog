package com.dailog.api.service.auth;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.request.oAuth2.MemberOAuth2Request;
import com.dailog.api.response.oAuth2.GoogleResponse;
import com.dailog.api.response.oAuth2.KakaoResponse;
import com.dailog.api.response.oAuth2.NaverResponse;
import com.dailog.api.response.oAuth2.OAuth2Response;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //OAuth2 서버로 전달된 유저 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);
        //OAuth2 공급자의 식별자
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        //유저의 속성 정보
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2Response oAuth2Response;
        switch (registrationId) {
            case "naver" -> oAuth2Response = new NaverResponse(attributes);
            case "google" -> oAuth2Response = new GoogleResponse(attributes);
            case "kakao" -> oAuth2Response = new KakaoResponse(attributes);
            default -> {
                return null;
            }
        }

        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getEmail();
        Optional<Member> existingMember = memberRepository.findByEmail(username);
        if (existingMember.isEmpty()) {

            Member member = Member.builder()
                    .email(username)
                    .name(oAuth2Response.getName())
                    .role(Role.MEMBER)
                    .nickname(oAuth2Response.getNickname())
                    .oAuth2Login(true)
                    .build();

            memberRepository.save(member);

            MemberOAuth2Request memberOAuth2Request = MemberOAuth2Request.builder()
                    .username(username)
                    .name(oAuth2Response.getName())
                    .role(Role.MEMBER)
                    .nickname(oAuth2Response.getNickname())
                    .provider(oAuth2Response.getProvider())
                    .build();

            return new CustomOAuth2User(memberOAuth2Request);
        } else {
            Member member = existingMember.get();
            memberRepository.save(member);

            MemberOAuth2Request memberOAuth2Request = MemberOAuth2Request.builder()
                    .username(member.getEmail())
                    .name(member.getName())
                    .role(member.getRole())
                    .nickname(member.getNickname())
                    .provider(oAuth2Response.getProvider())
                    .build();

            return new CustomOAuth2User(memberOAuth2Request);
        }
    }
}
