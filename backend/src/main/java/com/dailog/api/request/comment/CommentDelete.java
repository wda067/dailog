package com.dailog.api.request.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentDelete {

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(max = 1000, message = "1,000자 이상 입력할 수 없습니다.")
    private final String password;

    @JsonCreator
    public CommentDelete(String password) {
        this.password = password;
    }
}
