package com.dailog.api.request.oAuth2;

import com.dailog.api.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberOAuth2Request {

    private final String username;
    private final String name;
    private final Role role;
    private final String nickname;
    private final String provider;

    @Builder
    public MemberOAuth2Request(String username, String name, Role role, String nickname, String provider) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.nickname = nickname;
        this.provider = provider;
    }
}
