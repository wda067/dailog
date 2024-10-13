package com.dailog.api.response.likes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;

@Getter
public class LikesMemberResponse {

    private final String nickname;
    private final String likedAt;

    public LikesMemberResponse(LikesMemberResponse other) {
        this.nickname = other.getNickname();
        this.likedAt = other.getLikedAt();
    }

    public LikesMemberResponse(String nickname, LocalDateTime likedAt) {
        this.nickname = nickname;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime utcLikedAt = likedAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulLikedAt = utcLikedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        this.likedAt = seoulLikedAt.format(formatter);
    }
}
