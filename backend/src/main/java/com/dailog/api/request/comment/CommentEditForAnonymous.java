package com.dailog.api.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentEditForAnonymous {

    @NotBlank(message = "댓글을 입력해 주세요.")
    @Size(max = 1000, message = "댓글은 1,000자까지 입력해 주세요.")
    private final String content;

    private String password;
}
