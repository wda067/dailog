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
public class PostDetailResponse {

    private final Long id;
    private final Long memberId;
    private final String nickname;
    private final String title;
    private final String content;
    private final String createdAt;
    private final String createdBy;
    private final long commentCount;
    private final long views;

    public PostDetailResponse(Post post) {
        this.id = post.getId();
        this.memberId = post.getMemberId();
        this.title = post.getTitle().substring(0, Math.min(post.getTitle().length(), 60));
        this.content = post.getContent();
        this.nickname = post.getMember().getNickname();
        this.commentCount = post.getComments().size();
        this.views = post.getViews();

        ZonedDateTime utcCreatedAt = post.getCreatedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.createdBy = post.getCreatedBy();
    }

    public PostDetailResponse(Post post, long views) {
        this.id = post.getId();
        this.memberId = post.getMemberId();
        this.title = post.getTitle().substring(0, Math.min(post.getTitle().length(), 60));
        this.content = post.getContent();
        this.nickname = post.getMember().getNickname();
        this.commentCount = post.getComments().size();
        this.views = views;

        ZonedDateTime utcCreatedAt = post.getCreatedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.createdBy = post.getCreatedBy();
    }

    @Builder
    public PostDetailResponse(Long id, Long memberId, String nickname, String title, String content,
                              LocalDateTime createdAt, String createdBy, long commentCount, long views) {
        this.id = id;
        this.memberId = memberId;
        this.nickname = nickname;
        this.title = title.substring(0, Math.min(title.length(), 60));
        this.content = content;
        this.commentCount = commentCount;
        this.views = views;

        ZonedDateTime utcCreatedAt = createdAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.createdBy = createdBy;
    }
}
