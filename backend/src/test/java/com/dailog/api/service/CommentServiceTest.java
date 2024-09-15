package com.dailog.api.service;

import static com.dailog.api.domain.enums.Role.MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dailog.api.domain.Comment;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.comment.ForbiddenCommentAccess;
import com.dailog.api.exception.comment.InvalidCommentPassword;
import com.dailog.api.repository.comment.CommentRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.comment.CommentCreateForAnonymous;
import com.dailog.api.request.comment.CommentCreateForMember;
import com.dailog.api.request.comment.CommentDelete;
import com.dailog.api.request.comment.CommentEditForAnonymous;
import com.dailog.api.request.comment.CommentEditForMember;
import com.dailog.api.response.comment.CommentResponse;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void clean() {
        memberRepository.deleteAll();
        postRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 댓글 작성")
    void should_SaveMemberComment_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        CommentCreateForMember request = new CommentCreateForMember("내용");

        //when
        commentService.writeByMember(post.getId(), request, member.getEmail());

        //then
        assertEquals(1L, commentRepository.count());
        Comment comment = commentRepository.findAll().get(0);
        assertEquals("내용", comment.getContent());
    }

    @Test
    @DisplayName("비회원 댓글 작성")
    void should_SaveAnonymousComment_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        CommentCreateForAnonymous request = CommentCreateForAnonymous.builder()
                .anonymousName("비회원")
                .password("1234")
                .content("댓글 내용")
                .build();

        //when
        commentService.writeByAnonymous(post.getId(), request);

        //then
        assertEquals(1L, commentRepository.count());
        Comment comment = commentRepository.findAll().get(0);
        assertEquals("비회원", comment.getAnonymousName());
        assertTrue(passwordEncoder.matches("1234", comment.getPassword()));
        assertEquals("댓글 내용", comment.getContent());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("댓글 조회")
    void should_GetComments_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        List<Comment> memberComments = IntStream.range(1, 3)
                .mapToObj(i -> Comment.builder()
                        .content("회원 댓글 내용" + i)
                        .member(member)
                        .post(post)
                        .build()
                )
                .toList();
        commentRepository.saveAll(memberComments);

        List<Comment> guestComments = IntStream.range(1, 3)
                .mapToObj(i -> Comment.builder()
                        .anonymousName("비회원")
                        .password("1234")
                        .content("비회원 댓글 내용" + i)
                        .post(post)
                        .build())
                .toList();
        commentRepository.saveAll(guestComments);

        //when
        List<CommentResponse> commentResponses = commentService.get(post.getId());

        //then
        assertEquals(4L, commentResponses.size());
        assertEquals("회원 댓글 내용1", commentResponses.get(0).getContent());
        assertEquals("비회원 댓글 내용2", commentResponses.get(3).getContent());
    }

    @Test
    @DisplayName("회원 댓글 수정")
    void should_EditMemberComment_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .content("댓글 수정 전")
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForMember request = new CommentEditForMember("댓글 수정 후");

        //when
        commentService.editByMember(comment.getId(), request, member.getEmail());

        //then
        Comment editedComment = commentRepository.findByPostId(post.getId()).get(0);
        assertEquals("댓글 수정 후", editedComment.getContent());
    }

    @Test
    @DisplayName("회원 댓글 수정 실패 - 권한 없음")
    void should_FailToEditMemberComment_When_Unauthorized() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .content("댓글 수정 전")
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForMember request = new CommentEditForMember("댓글 수정 후");

        Member logInMember = Member.builder()
                .email("logIn@logIn.com")
                .password("1234")
                .role(MEMBER)
                .build();
        memberRepository.save(logInMember);

        //expected
        assertThrows(ForbiddenCommentAccess.class, () ->
                commentService.editByMember(comment.getId(), request, logInMember.getEmail()));
    }

    @Test
    @DisplayName("비회원 댓글 수정")
    void should_EditAnonymousComment_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .anonymousName("비회원")
                .password(passwordEncoder.encode("1234"))
                .content("댓글 수정 전")
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForAnonymous request = CommentEditForAnonymous.builder()
                .content("댓글 수정 후")
                .password("1234")
                .build();
        //when
        commentService.editByAnonymous(comment.getId(), request);

        //then
        Comment editedComment = commentRepository.findByPostId(post.getId()).get(0);
        assertEquals("댓글 수정 후", editedComment.getContent());
    }

    @Test
    @DisplayName("비회원 댓글 수정 실패 - 유효하지 않은 비밀번호")
    void should_FailToEditAnonymousComment_When_WrongPassword() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .anonymousName("비회원")
                .password(passwordEncoder.encode("1234"))
                .content("댓글 수정 전")
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentEditForAnonymous request = CommentEditForAnonymous.builder()
                .content("댓글 수정 후")
                .password("123456")
                .build();
        //expected
        assertThrows(InvalidCommentPassword.class, () ->
                commentService.editByAnonymous(comment.getId(), request));
    }

    @Test
    @DisplayName("회원 댓글 삭제")
    void should_DeleteMemberComment_When_Authorized() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .content("댓글 수정 전")
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        //when
        commentService.deleteMemberComment(comment.getId(), member.getEmail());

        //then
        assertEquals(0, commentRepository.count());
    }

    @Test
    @DisplayName("회원 댓글 삭제 실패 - 권한 없음")
    void should_FailToDeleteMemberComment_When_Unauthorized() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .content("댓글 수정 전")
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        Member logInMember = Member.builder()
                .email("logIn@logIn.com")
                .password("1234")
                .role(MEMBER)
                .build();
        memberRepository.save(logInMember);

        //expected
        assertThrows(ForbiddenCommentAccess.class, () ->
                commentService.deleteMemberComment(comment.getId(), logInMember.getEmail()));
    }

    @Test
    @DisplayName("비회원 댓글 삭제")
    void should_DeleteAnonymousComment_When_ValidPassword() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .anonymousName("비회원")
                .password(passwordEncoder.encode("1234"))
                .content("댓글 수정 전")
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentDelete request = new CommentDelete("1234");

        //when
        commentService.deleteAnonymousComment(comment.getId(), request);

        //then
        assertEquals(0, commentRepository.count());
    }

    @Test
    @DisplayName("비회원 댓글 삭제 실패 - 유효하지 않은 비밀번호")
    void should_FailToDeleteAnonymousComment_When_InvalidPassword() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        Comment comment = Comment.builder()
                .anonymousName("비회원")
                .password(passwordEncoder.encode("1234"))
                .content("댓글 수정 전")
                .post(post)
                .build();
        commentRepository.save(comment);
        post.addComment(comment);

        CommentDelete request = new CommentDelete("123456");

        //expected
        assertThrows(InvalidCommentPassword.class, () ->
                commentService.deleteAnonymousComment(comment.getId(), request));
    }

    private Member getMember() {
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .name("test")
                .email("test@test.com")
                .password(encryptedPassword)
                .role(MEMBER)
                .build();
        memberRepository.save(member);
        return member;
    }

    private Post getPost(Member member) {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        postRepository.save(post);
        return post;
    }

}