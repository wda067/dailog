package com.dailog.api.response.post;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostIdResponse {

    private final Long id;

    public PostIdResponse(Long id) {
        this.id = id;
    }
}
