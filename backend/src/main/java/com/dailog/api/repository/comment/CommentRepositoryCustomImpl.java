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

    //@Override
    //public Page<Comment> getList(Long postId, CommentPageRequest commentPageRequest) {
    //
    //    Long totalCount = queryFactory.select(comment.count())
    //            .from(comment)
    //            .where(comment.post.id.eq(postId))
    //            .fetchFirst();
    //
    //    // 먼저 부모 댓글을 페이징 처리합니다.
    //    List<Comment> parentComments = queryFactory.selectFrom(comment)
    //            .where(comment.post.id.eq(postId)
    //                    .and(comment.parentComment.isNull())) // 부모 댓글만 가져옵니다.
    //            .orderBy(comment.createdAt.asc()) // 부모 댓글의 작성 시간 순으로 정렬
    //            .fetch();
    //
    //    // 부모 댓글에 대응하는 자식 댓글들을 가져옵니다.
    //    List<Comment> allComments = new ArrayList<>();
    //    for (Comment parentComment : parentComments) {
    //        allComments.add(parentComment); // 부모 댓글 추가
    //        List<Comment> childComments = queryFactory.selectFrom(comment)
    //                .where(comment.parentComment.eq(parentComment)) // 자식 댓글만 가져옴
    //                .orderBy(comment.createdAt.asc()) // 작성 시간 순으로 정렬
    //                .limit(9) // 자식 댓글을 9개까지만 가져옵니다.
    //                .fetch();
    //        allComments.addAll(childComments); // 자식 댓글 추가
    //    }
    //
    //    //List<Comment> comments = queryFactory.selectFrom(comment)
    //    //        .where(comment.post.id.eq(postId))
    //    //        .orderBy(comment.parentComment.id.asc().nullsFirst(),
    //    //                comment.createdAt.asc())
    //    //        .limit(commentPageRequest.getSize())
    //    //        .offset(commentPageRequest.getOffset())
    //    //        .fetch();
    //
    //    //List<Comment> parentComments = queryFactory.selectFrom(comment)
    //    //        .where(comment.post.id.eq(postId)
    //    //                .and(comment.parentComment.isNull()))
    //    //        .limit(commentPageRequest.getSize())
    //    //        .offset(commentPageRequest.getOffset())
    //    //        .fetch();
    //    //
    //    //List<Long> parentCommentIds = parentComments.stream()
    //    //        .map(Comment::getId)
    //    //        .toList();
    //    //
    //    //Map<Long, List<Comment>> childCommentMap = queryFactory.selectFrom(comment)
    //    //        .where(comment.parentComment.id.in(parentCommentIds))
    //    //        .fetch()
    //    //        .stream()
    //    //        .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));
    //    //
    //    //parentComments.forEach(parent -> {
    //    //    List<Comment> childComments = childCommentMap.getOrDefault(parent.getId(), List.of());
    //    //    childComments.forEach(parent::addChildComment);
    //    //});
    //
    //    return new PageImpl<>(allComments, commentPageRequest.getPageable(), totalCount);
    //}
}
