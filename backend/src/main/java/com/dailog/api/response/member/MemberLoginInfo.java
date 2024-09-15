package com.dailog.api.response.member;

import com.dailog.api.domain.Member;
import lombok.Getter;

@Getter
public class MemberLoginInfo {

    private final Long id;
    private final String name;
    private final String nickname;
    private final String role;

    public MemberLoginInfo(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.role = member.getRole().name();
    }
}
