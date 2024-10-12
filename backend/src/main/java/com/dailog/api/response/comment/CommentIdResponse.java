package com.dailog.api.response.comment;

import lombok.Getter;

@Getter
public class CommentIdResponse {

    private final String id;

    public CommentIdResponse(Long id) {
        this.id = String.valueOf(id);
    }
}
