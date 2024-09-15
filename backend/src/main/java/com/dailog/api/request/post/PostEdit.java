package com.dailog.api.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PostEdit {

    @NotBlank(message = "제목을 입력해 주세요.")
    private final String title;

    @NotBlank(message = "내용를 입력해 주세요.")
    private final String content;

    @Builder
    public PostEdit(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
