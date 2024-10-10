package com.dailog.api.repository.comment;

import com.dailog.api.domain.Comment;
import com.dailog.api.request.comment.CommentPageRequest;
import org.springframework.data.domain.Page;

public interface CommentRepositoryCustom {

    Page<Comment> getList(Long postId, CommentPageRequest commentPageRequest);

}
