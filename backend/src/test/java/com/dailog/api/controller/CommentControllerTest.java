package com.dailog.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dailog.api.config.CustomMockAdmin;
import com.dailog.api.config.CustomMockMember;
import com.dailog.api.domain.Comment;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.comment.CommentNotFound;
import com.dailog.api.repository.comment.CommentRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.comment.CommentCreateForAnonymous;
import com.dailog.api.request.comment.CommentCreateForMember;
import com.dailog.api.request.comment.CommentDelete;
import com.dailog.api.request.comment.CommentEditForAnonymous;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clean() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 댓글 작성")
    void should_SaveComment_When_LoggedIn() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        postRepository.save(post);

        CommentCreateForMember request = new CommentCreateForMember("댓글");
        String json = objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/posts/{postId}/comments/member", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        assertEquals(1L, commentRepository.count());
        Comment savedComment = commentRepository.findAll().get(0);
        assertEquals("댓글", savedComment.getContent());
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 잘못된 댓글 작성 - 공백 댓글")
    void should_ReturnBadRequest_When_ContentIsEmpty() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        postRepository.save(post);

        CommentCreateForMember request = new CommentCreateForMember("");
        String json = objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/posts/{postId}/comments/member", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("댓글을 입력해 주세요."))
                .andDo(print());
    }
    @Test
    @CustomMockMember
    @DisplayName("회원 잘못된 댓글 작성 - 1000자 이상의 댓글")
    void should_ReturnBadRequest_When_ContentIsTooLong() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        postRepository.save(post);

        String longContent = "a".repeat(1001);
        CommentCreateForMember request = new CommentCreateForMember(longContent);
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/posts/{postId}/comments/member", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.content").value("내용은 1000자까지 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 댓글 수정")
    void should_EditComment_When_LoggedIn() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        postRepository.save(post);

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content("댓글 수정 전")
                .build();

        commentRepository.save(comment);
        post.addComment(comment);

        CommentCreateForMember request = new CommentCreateForMember("댓글 수정 후");
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(patch("/api/comments/{commentId}/member", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        Comment editedComment = commentRepository.findById(comment.getId())
                .orElseThrow(CommentNotFound::new);

        assertEquals("댓글 수정 후", editedComment.getContent());
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 댓글 수정 실패 - 다른 작성자")
    void should_ReturnForbidden_When_EditingCommentByAnotherUser() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();

        postRepository.save(post);

        Comment comment = Comment.builder()
                .post(post)
                .member(getMember())
                .content("댓글 수정 전")
                .build();

        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForAnonymous request = CommentEditForAnonymous.builder()
                .content("댓글 수정 후")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(patch("/api/comments/{commentId}/member", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("이 댓글의 작성자가 아닙니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("비회원 댓글 작성")
    void should_SaveAnonymousComment_When_ValidRequest() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        CommentCreateForAnonymous request = CommentCreateForAnonymous.builder()
                .anonymousName("익명")
                .password("1234")
                .content("댓글")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/posts/{postId}/comments/anonymous", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        assertEquals(1L, commentRepository.count());
        Comment savedComment = commentRepository.findAll().get(0);
        assertEquals("익명", savedComment.getAnonymousName());
        assertTrue(passwordEncoder.matches("1234", savedComment.getPassword()));
        assertEquals("댓글", savedComment.getContent());
    }

    @Test
    @DisplayName("비회원 잘못된 댓글 작성 - 8자 이상의 이름")
    void should_ReturnBadRequest_When_NameTooLong() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        CommentCreateForAnonymous request = CommentCreateForAnonymous.builder()
                .anonymousName("8자 이상의 이름입니다.")
                .password("1234")
                .content("댓글")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/posts/{postId}/comments/anonymous", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.anonymousName").value("이름을 1~8자로 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("비회원 잘못된 댓글 작성 - 유효하지 않은 비밀번호")
    void should_ReturnBadRequest_When_InvalidPassword() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        CommentCreateForAnonymous request = CommentCreateForAnonymous.builder()
                .anonymousName("익명")
                .password("1")
                .content("댓글")
                .build();
        String json = objectMapper.writeValueAsString(request);

        // When
        mockMvc.perform(post("/api/posts/{postId}/comments/anonymous", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.password").value("비밀번호는 4~30자로 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("비회원 댓글 수정")
    void should_EditAnonymousComment_When_ValidRequest() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        String encryptedPassword = passwordEncoder.encode("1234");
        Comment comment = Comment.builder()
                .post(post)
                .anonymousName("익명")
                .password(encryptedPassword)
                .content("댓글 수정 전")
                .build();

        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForAnonymous request = CommentEditForAnonymous.builder()
                .password("1234")
                .content("댓글 수정 후")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(patch("/api/comments/{commentId}/anonymous", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        Comment editedComment = commentRepository.findById(comment.getId())
                .orElseThrow(CommentNotFound::new);

        assertEquals("댓글 수정 후", editedComment.getContent());
    }

    @Test
    @DisplayName("비회원 댓글 수정 실패 - 다른 비밀번호")
    void should_FailToEditAnonymousComment_When_WrongPassword() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        String encryptedPassword = passwordEncoder.encode("1234");
        Comment comment = Comment.builder()
                .post(post)
                .anonymousName("익명")
                .password(encryptedPassword)
                .content("댓글 수정 전")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForAnonymous request = CommentEditForAnonymous.builder()
                .password("4321")
                .content("댓글 수정 후")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(patch("/api/comments/{commentId}/anonymous", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("댓글 비밀번호가 잘못 되었습니다."))
                .andDo(print());
    }

    @Test
    @CustomMockAdmin
    @DisplayName("관리자 댓글 삭제")
    void should_DeleteCommentByAdmin_When_Authorized() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        String encryptedPassword = passwordEncoder.encode("1234");
        Comment comment = Comment.builder()
                .post(post)
                .anonymousName("익명")
                .password(encryptedPassword)
                .content("댓글")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        //expected
        mockMvc.perform(post("/api/admin/comments/{commentId}/delete", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 댓글 삭제")
    void should_DeleteMemberComment_When_Authorized() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content("댓글")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        //expected
        mockMvc.perform(post("/api/comments/{commentId}/delete/member", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("회원 댓글 삭제 실패 - 권한 없음")
    void should_ReturnForbidden_When_Unauthorized() throws Exception {
        //given
        Member another = getMember();
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(another)
                .build();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .post(post)
                .member(another)
                .content("댓글")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        //expected
        mockMvc.perform(post("/api/comments/{commentId}/delete/member", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("이 댓글의 작성자가 아닙니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("익명 댓글 삭제")
    void should_DeleteAnonymousComment_When_ValidRequest() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        String encryptedPassword = passwordEncoder.encode("1234");
        Comment comment = Comment.builder()
                .post(post)
                .anonymousName("익명")
                .password(encryptedPassword)
                .content("댓글")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentDelete commentDelete = new CommentDelete("1234");
        String json = objectMapper.writeValueAsString(commentDelete);
        //expected
        mockMvc.perform(post("/api/comments/{commentId}/delete/anonymous", comment.getId())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("익명 댓글 삭제 실패 - 틀린 비밀번호")
    void should_FailToDeleteAnonymousComment_When_WrongPassword() throws Exception {
        //given
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(getMember())
                .build();
        postRepository.save(post);

        String encryptedPassword = passwordEncoder.encode("1234");
        Comment comment = Comment.builder()
                .post(post)
                .anonymousName("익명")
                .password(encryptedPassword)
                .content("댓글")
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentDelete commentDelete = new CommentDelete("");
        String json = objectMapper.writeValueAsString(commentDelete);
        //expected
        mockMvc.perform(post("/api/comments/{commentId}/delete/anonymous", comment.getId())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("댓글 비밀번호가 잘못 되었습니다."))
                .andDo(print());
    }

    private Member getMember() {
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .name("test")
                .email("test@test.com")
                .password(encryptedPassword)
                .build();
        memberRepository.save(member);
        return member;
    }
}