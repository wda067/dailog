package com.dailog.api.repository.comment;

import com.dailog.api.domain.Comment;
import java.util.List;

public interface CommentRepositoryCustom {

    List<Comment> findByPostId(Long postId);
}
