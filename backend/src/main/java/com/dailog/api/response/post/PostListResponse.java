package com.dailog.api.response.post;

import com.dailog.api.domain.Post;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;

/**
 * 서비스 정책에 맞는 클래스
 */
@Getter
@Setter
public class PostListResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String createdAt;
    private final String nickname;
    private final long commentCount;
    private final long likes;
    private long views;

    public PostListResponse(PostListResponse other) {
        this.id = other.getId();
        this.title = other.getTitle();
        this.content = other.getContent();
        this.createdAt = other.getCreatedAt();
        this.nickname = other.getNickname();
        this.commentCount = other.getCommentCount();
        this.views = other.getViews();
        this.likes = other.getLikes();
    }

    public PostListResponse(Long id, String title, String content, LocalDateTime createdAt, String nickname,
                            long commentCount, long views, long likes) {
        this.id = id;
        this.title = title.substring(0, Math.min(title.length(), 60));
        this.content = content;
        this.nickname = nickname;
        this.commentCount = commentCount;
        this.views = views;
        this.likes = likes;

        ZonedDateTime utcCreatedAt = createdAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
    }
}
