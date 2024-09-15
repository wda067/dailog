package com.dailog.api.response.member;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberProfile {

    private final Long id;
    private final String email;
    private final String name;
    private final String nickname;
    private final String createdAt;
    private final String role;
    private final boolean oAuth2Login;

    public MemberProfile(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.oAuth2Login = member.isOAuth2Login();

        ZonedDateTime utcCreatedAt = member.getCreatedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.role = member.getRole().name();
    }

    //@Builder
    //public MemberProfile(Long id, String email, String name, String nickname, LocalDateTime createdAt, Role role,
    //                     boolean oAuth2Login) {
    //    this.id = id;
    //    this.email = email;
    //    this.name = name;
    //    this.nickname = nickname;
    //    this.oAuth2Login = oAuth2Login;
    //    ZonedDateTime zonedCreatedAt = createdAt.atZone(ZoneId.of("Asia/Seoul"));
    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //    this.createdAt = zonedCreatedAt.format(formatter);
    //    this.role = role.name();
    //}
}
