package com.dailog.api.controller;

import static com.dailog.api.domain.enums.Role.MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dailog.api.config.CustomMockMember;
import com.dailog.api.domain.Likes;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class LikesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private LikesRepository likesRepository;

    @AfterEach
    void setUp() {
        likesRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글에 좋아요룰 누르면 좋아요 수가 1 증가한다.")
    @CustomMockMember
    void should_LikePost_When_LoggedIn() throws Exception {
        //given
        Member member = Member.builder()
                .name("test")
                .email("test@test.com")
                .password("1234")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .member(member)
                .title("제목")
                .content("내용")
                .build();
        postRepository.save(post);

        //when
        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        assertEquals(1L, likesRepository.countByPostId(post.getId()));
    }

    @Test
    @DisplayName("동일한 게시글에는 1번만 좋아요를 누를 수 있다.")
    @CustomMockMember
    void should_NotAllowDuplicateLikes_When_SamePost() throws Exception {
        //given
        Member loggedInMember = memberRepository.findAll().get(0);
        Member anotherMember = Member.builder()
                .name("test")
                .email("test@test.com")
                .password("1234")
                .role(MEMBER)
                .build();
        memberRepository.save(anotherMember);

        Post post = Post.builder()
                .member(anotherMember)
                .title("제목")
                .content("내용")
                .build();
        postRepository.save(post);

        Likes likes = Likes.builder()
                .member(loggedInMember)
                .post(post)
                .build();
        likesRepository.save(likes);

        //when
        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());

        //then
        assertEquals(1L, likesRepository.countByPostId(post.getId()));
    }

    @Test
    @DisplayName("좋아요를 취소하면 게시글의 좋아요 수가 1 감소한다.")
    @CustomMockMember
    void should_DecreaseLikes_When_LikeIsCancelled() throws Exception {
        //given
        Member loggedInMember = memberRepository.findAll().get(0);
        Member anotherMember = Member.builder()
                .name("test")
                .email("test@test.com")
                .password("1234")
                .role(MEMBER)
                .build();
        memberRepository.save(anotherMember);

        Post post = Post.builder()
                .member(anotherMember)
                .title("제목")
                .content("내용")
                .build();
        postRepository.save(post);

        Likes likes = Likes.builder()
                .member(loggedInMember)
                .post(post)
                .build();
        likesRepository.save(likes);

        //when
        mockMvc.perform(delete("/api/posts/{postId}/likes", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        assertEquals(0L, likesRepository.countByPostId(post.getId()));
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글에 좋아요를 누른 회원 목록을 조회한다.")
    void should_GetMembersWhoLiked_When_ValidRequest() throws Exception {
        //given
        Member loggedInMember = memberRepository.findAll().get(0);
        Post post = Post.builder()
                .member(loggedInMember)
                .title("제목")
                .content("내용")
                .build();
        postRepository.save(post);

        for (int i = 1; i <= 50; i++) {
            Member member = Member.builder()
                    .nickname(i+"번째 회원")
                    .email("member@test.com" + i)
                    .password("1234")
                    .role(MEMBER)
                    .build();
            memberRepository.save(member);

            Likes likes = Likes.builder()
                    .member(member)
                    .post(post)
                    .build();
            likesRepository.save(likes);
        }

        //when
        mockMvc.perform(get("/api/posts/{postId}/likes/members?page=0&size=10", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.[0].nickname").value("50번째 회원"))
                .andExpect(jsonPath("$.items.[9].nickname").value("41번째 회원"))
                .andDo(print());
    }
}