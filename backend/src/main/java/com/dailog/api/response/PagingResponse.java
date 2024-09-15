package com.dailog.api.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PagingResponse<T> {

    private final long page;
    private final long size;
    private final long totalCount;
    private final List<T> items;

    public PagingResponse(Page<?> page, Class<T> entityClass) {
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalCount = page.getTotalElements();
        this.items = page.getContent().stream()
                .map(content -> {
                    try {
                        return entityClass.getConstructor(content.getClass()).newInstance(content);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
}
