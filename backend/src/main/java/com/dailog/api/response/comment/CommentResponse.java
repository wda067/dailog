package com.dailog.api.response.comment;

import com.dailog.api.domain.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 서비스 정책에 맞는 클래스
 */
@Getter
public class CommentResponse {

    private final Long id;
    private final String memberId;
    private final String nickname;
    private final String anonymousName;
    private final String password;
    private final String content;
    private final String createdAt;
    private final String updatedAt;
    private final String ipAddress;
    @JsonProperty("isParent")
    private final boolean isParent;
    //private final List<CommentResponse> childComments;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        if (comment.getMember() != null) {
            this.nickname = comment.getMember().getNickname();
            this.memberId = String.valueOf(comment.getMemberId());
        } else {
            this.nickname = "";
            this.memberId = "";
        }
        this.anonymousName = comment.getAnonymousName();
        this.password = comment.getPassword();
        this.content = comment.getContent();
        if (comment.getIpAddress() != null) {
            String[] split = comment.getIpAddress().split("\\.");
            this.ipAddress = split[0] + "." + split[1];
        } else {
            this.ipAddress = "";
        }
        //this.childComments = comment.getChildComments().stream()
        //        .map(CommentResponse::new)
        //        .collect(Collectors.toList());

        this.isParent = comment.getParentComment() == null;

        ZonedDateTime utcCreatedAt = comment.getCreatedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.updatedAt = comment.getUpdatedAt().format(formatter);
    }

    @Builder
    public CommentResponse(Long id, String nickname, Long memberId, String anonymousName, String password,
                           String content, LocalDateTime createdAt, LocalDateTime updatedAt, String ipAddress,
                           List<CommentResponse> childComments, boolean isParent) {
        this.id = id;
        this.nickname = nickname;
        this.memberId = String.valueOf(memberId);
        this.anonymousName = anonymousName;
        this.password = password;
        this.content = content;
        this.isParent = isParent;
        if (ipAddress != null) {
            String[] split = ipAddress.split("\\.");
            this.ipAddress = split[0] + "." + split[1];
        } else {
            this.ipAddress = "";
        }
        //this.childComments = childComments == null ? new ArrayList<>() : childComments;

        ZonedDateTime utcCreatedAt = createdAt.atZone(ZoneId.of("UTC"));
        ZonedDateTime seoulCreatedAt = utcCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = seoulCreatedAt.format(formatter);
        this.updatedAt = updatedAt.format(formatter);
    }
}
