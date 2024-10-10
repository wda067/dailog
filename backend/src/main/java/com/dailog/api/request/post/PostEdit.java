package com.dailog.api.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PostEdit {

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 100, message = "제목은 100자까지 입력해 주세요.")
    private final String title;

    @NotBlank(message = "내용를 입력해 주세요.")
    @Size(max = 10000, message = "내용은 10,000자까지 입력해 주세요.")
    private final String content;

    @Builder
    public PostEdit(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
