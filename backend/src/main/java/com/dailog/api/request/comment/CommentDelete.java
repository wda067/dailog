package com.dailog.api.request.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public class CommentDelete {

    private final String password;

    @JsonCreator
    public CommentDelete(String password) {
        this.password = password;
    }
}
