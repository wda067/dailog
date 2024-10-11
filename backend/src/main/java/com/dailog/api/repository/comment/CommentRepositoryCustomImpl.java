package com.dailog.api.repository.comment;

import static com.dailog.api.domain.QComment.comment;

import com.dailog.api.domain.Comment;
import com.dailog.api.request.comment.CommentPageRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Comment> getList(Long postId, CommentPageRequest commentPageRequest) {

        //Long totalCount = queryFactory.select(comment.count())
        //        .from(comment)
        //        .where(comment.post.id.eq(postId))
        //        .fetchFirst();

        //부모 댓글 조회
        List<Comment> parentComments = queryFactory.selectFrom(comment)
                .where(comment.post.id.eq(postId)
                        .and(comment.parentComment.isNull()))
                .orderBy(comment.createdAt.asc())
                .fetch();

        List<Comment> allComments = new ArrayList<>();
        for (Comment parentComment : parentComments) {
            //부모 댓글의 자식 댓글 조회
            List<Comment> childComments = queryFactory.selectFrom(comment)
                    .where(comment.parentComment.eq(parentComment))
                    .orderBy(comment.createdAt.asc())
                    .fetch();

            //부모 -> 자식 순으로 리스트에 추가
            allComments.add(parentComment);
            allComments.addAll(childComments);
        }

        int start = (int) commentPageRequest.getOffset();
        int end = Math.min((start + commentPageRequest.getSize()), allComments.size());

        //페이징된 결과 리스트 생성
        List<Comment> pagedComments = allComments.subList(start, end);

        return new PageImpl<>(pagedComments, commentPageRequest.getPageable(), allComments.size());
    }
}
