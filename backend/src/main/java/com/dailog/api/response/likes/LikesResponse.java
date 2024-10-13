package com.dailog.api.response.likes;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LikesResponse {

    private final long likes;
    private final boolean likedStatus;

    @Builder
    public LikesResponse(long likes, boolean likedStatus) {
        this.likes = likes;
        this.likedStatus = likedStatus;
    }
}
