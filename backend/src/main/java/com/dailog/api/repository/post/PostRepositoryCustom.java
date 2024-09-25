package com.dailog.api.repository.post;


import com.dailog.api.domain.Post;
import com.dailog.api.request.post.PostPageRequest;
import com.dailog.api.request.post.PostSearch;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface PostRepositoryCustom {

    Page<Post> getList(PostPageRequest postPageRequest);

    Page<Post> getList(PostSearch postSearch, PostPageRequest postPageRequest);

    Optional<Post> findPrevPost(Long id);

    Optional<Post> findNextPost(Long id);

    Long findPrevPostId(Long id);

    Long findNextPostId(Long id);
}
