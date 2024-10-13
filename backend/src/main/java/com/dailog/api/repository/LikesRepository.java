package com.dailog.api.repository;

import com.dailog.api.domain.Likes;
import com.dailog.api.response.likes.LikesMemberResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Optional<Likes> findByMemberIdAndPostId(Long memberId, Long postId);

    @Query("select count(l) "
            + "from Likes l "
            + "where l.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    @Query("select new com.dailog.api.response.likes.LikesMemberResponse(l.member.nickname, l.createdAt) "
            + "from Likes l "
            + "where l.post.id = :postId "
            + "order by l.createdAt desc")
    Page<LikesMemberResponse> findMembersByPostId(@Param("postId") Long postId, Pageable pageRequest);
}
