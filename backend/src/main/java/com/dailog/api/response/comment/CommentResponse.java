package com.dailog.api.response.comment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

/**
 * 서비스 정책에 맞는 클래스
 */
@Getter
public class CommentResponse {

    private final Long id;
    private final Long memberId;
    private final String nickname;
    private final String anonymousName;
    private final String password;
    private final String content;
    private final String createdAt;
    private final String updatedAt;

    @Builder
    public CommentResponse(Long id, String nickname, Long memberId, String anonymousName, String password,
                           String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.memberId = memberId;
        this.anonymousName = anonymousName;
        this.password = password;
        this.content = content;

        ZonedDateTime utcCreatedAt = createdAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.updatedAt = updatedAt.format(formatter);
    }
}
