package com.dailog.api.repository.post;

import static com.dailog.api.domain.QLikes.likes;
import static com.dailog.api.domain.QPost.post;
import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.jpa.JPAExpressions.select;

import com.dailog.api.domain.Post;
import com.dailog.api.domain.QPost;
import com.dailog.api.request.post.PostPageRequest;
import com.dailog.api.request.post.PostSearch;
import com.dailog.api.response.post.PostListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> getList(PostPageRequest postPageRequest) {

        Long totalCount = queryFactory.select(post.count())
                .from(post)
                .fetchFirst();

        List<Post> posts = queryFactory.selectFrom(post)
                .limit(postPageRequest.getSize())
                .offset(postPageRequest.getOffset())
                .orderBy(post.id.desc())
                .fetch();

        return new PageImpl<>(posts, postPageRequest.getPageable(), totalCount);
    }

    @Override
    public Page<PostListResponse> getList(PostSearch postSearch, PostPageRequest postPageRequest) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(regDtsAfter(postSearch.getSearchDateType()));
        builder.and(searchByLike(postSearch.getSearchQuery(), postSearch.getSearchType()));

        Long count = queryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchFirst();

        List<PostListResponse> posts = queryFactory.select(
                        constructor(PostListResponse.class,
                                post.id,
                                post.title,
                                post.content,
                                post.createdAt,
                                post.member.nickname,
                                post.comments.size().longValue(),
                                post.views,
                                select(likes.count())
                                        .from(likes)
                                        .where(likes.post.eq(post))))
                .from(post)
                .where(builder)
                .limit(postPageRequest.getSize())
                .offset(postPageRequest.getOffset())
                .orderBy(post.id.desc())
                .fetch();

        return new PageImpl<>(posts, postPageRequest.getPageable(), count);
    }

    //@Override
    //public Page<Post> getList(PostSearch postSearch, PostPageRequest postPageRequest) {
    //
    //    BooleanBuilder builder = new BooleanBuilder();
    //    builder.and(regDtsAfter(postSearch.getSearchDateType()));
    //    builder.and(searchByLike(postSearch.getSearchQuery(), postSearch.getSearchType()));
    //
    //    Long count = queryFactory.select(post.count())
    //            .from(post)
    //            .where(builder)
    //            .fetchFirst();
    //
    //    List<Post> posts = queryFactory.selectFrom(post)
    //            .where(builder)
    //            .limit(postPageRequest.getSize())
    //            .offset(postPageRequest.getOffset())
    //            .orderBy(post.id.desc())
    //            .fetch();
    //
    //    return new PageImpl<>(posts, postPageRequest.getPageable(), count);
    //}

    @Override
    public Optional<Post> findPrevPost(Long id) {

        Post post = queryFactory.selectFrom(QPost.post)
                .where(QPost.post.id.lt(id))
                .orderBy(QPost.post.id.desc())
                .fetchFirst();

        return Optional.ofNullable(post);
    }

    @Override
    public Optional<Post> findNextPost(Long id) {

        Post post = queryFactory.selectFrom(QPost.post)
                .where(QPost.post.id.gt(id))
                .orderBy(QPost.post.id.asc())
                .fetchFirst();

        return Optional.ofNullable(post);
    }

    @Override
    public Long findPrevPostId(Long id) {

        return queryFactory.select(post.id)
                .from(post)
                .where(post.id.lt(id))
                .orderBy(post.id.desc())
                .fetchFirst();
    }

    @Override
    public Long findNextPostId(Long id) {

        return queryFactory.select(post.id)
                .from(post)
                .where(post.id.gt(id))
                .orderBy(post.id.asc())
                .fetchFirst();
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (searchDateType == null) {
            return null;
        }

        switch (searchDateType) {
            case "1w" -> dateTime = dateTime.minusWeeks(1);
            case "1m" -> dateTime = dateTime.minusMonths(1);
            case "6m" -> dateTime = dateTime.minusMonths(6);
            case "1y" -> dateTime = dateTime.minusYears(1);
            default -> {
                return null;
            }
        }

        return post.createdAt.after(dateTime)
                .or(post.createdAt.eq(dateTime));
    }

    private BooleanExpression searchByLike(String searchQuery, String searchType) {
        if (searchQuery == null || searchType == null) {
            return null;
        }

        BooleanExpression titleExpression = post.title.like("%" + searchQuery + "%");
        BooleanExpression contentExpression = post.content.like("%" + searchQuery + "%");

        switch (searchType) {
            case "title" -> {
                return titleExpression;
            }
            case "content" -> {
                return contentExpression;
            }
            case "titleOrContent" -> {
                return titleExpression.or(contentExpression);
            }
            default -> {
                return null;
            }
        }
    }
}
