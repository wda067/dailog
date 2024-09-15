package com.dailog.api.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PostCreate {

    @NotBlank(message = "제목을 입력해 주세요.")
    private final String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private final String content;

    //빌더 패턴의 장점
    //1. 가독성
    //2. 필요한 값만 받을 수 있다.
    //3. 객체의 불변성
    @Builder
    public PostCreate(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
