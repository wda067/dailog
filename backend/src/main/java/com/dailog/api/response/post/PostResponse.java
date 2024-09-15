package com.dailog.api.response.post;

import com.dailog.api.domain.Post;
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
public class PostResponse {

    private final Long id;
    private final Long memberId;
    private final String name;
    private final String nickname;
    private final String title;
    private final String content;
    private final String createdAt;
    private final String createdBy;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.memberId = post.getMemberId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.name = post.getMember().getName();
        this.nickname = post.getMember().getNickname();

        ZonedDateTime utcCreatedAt = post.getCreatedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.createdBy = post.getCreatedBy();
    }

    @Builder
    public PostResponse(Long id, Long memberId, String name, String nickname, String title, String content,
                        LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.nickname = nickname;
        this.title = title.substring(0, Math.min(title.length(), 60));
        this.content = content;

        ZonedDateTime utcCreatedAt = createdAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        //this.updatedAt = updatedAt.format(formatter);
        this.createdBy = createdBy;
    }
}
