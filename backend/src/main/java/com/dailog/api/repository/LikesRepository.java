package com.dailog.api.repository;

import com.dailog.api.domain.Likes;
import java.util.Optional;
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
}