package com.dailog.api.request.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentEditForMember {

    @NotBlank(message = "내용를 입력해 주세요.")
    private final String content;

    @JsonCreator
    public CommentEditForMember(String content) {
        this.content = content;
    }
}
