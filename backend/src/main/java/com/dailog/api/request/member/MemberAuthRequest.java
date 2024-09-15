package com.dailog.api.request.member;

import com.dailog.api.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberAuthRequest {

    private final String username;
    private final String name;
    private final Role role;
    private final String nickname;

    @Builder
    public MemberAuthRequest(String username, String name, Role role, String nickname) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.nickname = nickname;
    }
}
