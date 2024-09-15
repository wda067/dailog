package com.dailog.api.request.post;

import static java.lang.Math.max;
import static java.lang.Math.min;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
public class PostPageRequest {

    public static final int MAX_SIZE = 2_000;

    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;

    //@Builder
    //public PostSearch(Integer page, Integer size) {
    //    this.page = page == null ? 1 : page;
    //    this.size = size == null ? 20 : size;
    //}

    public long getOffset() {
        //페이지가 0일 때 첫 번째 페이지
        return (long) (max(1, page) - 1) * min(size, MAX_SIZE);
    }

    public Pageable getPageable() {
        return PageRequest.of(max(1, page) - 1, min(size, MAX_SIZE));
    }
}
