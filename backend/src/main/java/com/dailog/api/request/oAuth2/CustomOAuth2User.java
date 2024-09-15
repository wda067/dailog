package com.dailog.api.request.oAuth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final MemberOAuth2Request memberOAuth2Request;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return memberOAuth2Request.getRole().getAuthorities();
    }

    //@Override
    //public Collection<? extends GrantedAuthority> getAuthorities() {
    //    Collection<GrantedAuthority> authorities = new ArrayList<>();
    //    authorities.add(new SimpleGrantedAuthority(memberOAuth2Request.getRole()));
    //    return authorities;
    //}

    @Override
    public String getName() {
        return memberOAuth2Request.getName();
    }

    public String getUsername() {
        return memberOAuth2Request.getUsername();
    }

    public String getNickname() {
        return memberOAuth2Request.getNickname();
    }

    public String getProvider() {
        return memberOAuth2Request.getProvider();
    }

}
