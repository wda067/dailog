package com.dailog.api.response.likes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LikesResponse {

    private final long likes;
    @JsonProperty("isLiked")
    private final boolean isLiked;

    @Builder
    public LikesResponse(long likes, boolean isLiked) {
        this.likes = likes;
        this.isLiked = isLiked;
    }
}
