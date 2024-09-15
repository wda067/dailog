package com.dailog.api.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentEditForAnonymous {

    @NotBlank(message = "내용를 입력해 주세요.")
    private final String content;

    private String password;
}
