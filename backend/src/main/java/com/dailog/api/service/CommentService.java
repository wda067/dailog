package com.dailog.api.service;

import com.dailog.api.domain.Comment;
import com.dailog.api.domain.CommentEditor;
import com.dailog.api.domain.CommentEditor.CommentEditorBuilder;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.comment.CommentNotFound;
import com.dailog.api.exception.comment.ForbiddenCommentAccess;
import com.dailog.api.exception.comment.InvalidCommentPassword;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.exception.post.PostNotFound;
import com.dailog.api.repository.comment.CommentRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.comment.CommentCreateForAnonymous;
import com.dailog.api.request.comment.CommentCreateForMember;
import com.dailog.api.request.comment.CommentDelete;
import com.dailog.api.request.comment.CommentEditForAnonymous;
import com.dailog.api.request.comment.CommentEditForMember;
import com.dailog.api.response.comment.CommentResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void writeByMember(Long postId, CommentCreateForMember request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(request.getContent())
                .build();

        post.addComment(comment);
    }

    @Transactional
    public void writeByAnonymous(Long postId, CommentCreateForAnonymous request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        Comment comment = Comment.builder()
                .post(post)
                .anonymousName(request.getAnonymousName())
                .password(passwordEncoder.encode(request.getPassword()))
                .content(request.getContent())
                .build();

        post.addComment(comment);
    }

    public List<CommentResponse> get(Long postId, HttpServletRequest request) {
        List<Comment> comments = commentRepository.findByPostId(postId);

        return comments.stream()
                .map(comment -> {
                            if (comment.getMember() == null) {
                                return CommentResponse.builder()
                                        .id(comment.getId())
                                        .anonymousName(comment.getAnonymousName())
                                        .password(comment.getPassword())
                                        .content(comment.getContent())
                                        .createdAt(comment.getCreatedAt())
                                        .updatedAt(comment.getUpdatedAt())
                                        .ipAddress(request.getRemoteAddr())
                                        .build();
                            }
                            return CommentResponse.builder()
                                    .id(comment.getId())
                                    .memberId(comment.getMemberId())
                                    .nickname(comment.getMemberNickname())
                                    .content(comment.getContent())
                                    .createdAt(comment.getCreatedAt())
                                    .updatedAt(comment.getUpdatedAt())
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    @Transactional
    public void editByMember(Long commentId, CommentEditForMember request, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);

        Long memberId = comment.getMemberId();
        validateWriter(memberId, email);

        CommentEditorBuilder commentEditorBuilder = comment.toEditor();
        if (request.getContent() != null) {
            commentEditorBuilder.content(request.getContent());
        }
        CommentEditor commentEditor = commentEditorBuilder.build();
        comment.edit(commentEditor);
    }

    @Transactional
    public void editByAnonymous(Long commentId, CommentEditForAnonymous request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        validatePassword(request.getPassword(), comment);

        CommentEditorBuilder commentEditorBuilder = comment.toEditor();
        if (request.getContent() != null) {
            commentEditorBuilder.content(request.getContent());
        }
        CommentEditor commentEditor = commentEditorBuilder.build();
        comment.edit(commentEditor);
    }

    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        commentRepository.delete(comment);
    }

    public void deleteMemberComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        Long memberId = comment.getMemberId();
        validateWriter(memberId, email);
        commentRepository.delete(comment);
    }

    public void deleteAnonymousComment(Long commentId, CommentDelete request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        validatePassword(request.getPassword(), comment);
        commentRepository.delete(comment);
    }

    private void validatePassword(String rawPassword, Comment comment) {
        if (rawPassword == null) {
            throw new InvalidCommentPassword();
        }
        if (!passwordEncoder.matches(rawPassword, comment.getPassword())) {
            throw new InvalidCommentPassword();
        }
    }

    private void validateWriter(Long memberId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);

        boolean isNotAuthor = !member.getId().equals(memberId);
        if (isNotAuthor) {
            throw new ForbiddenCommentAccess();
        }
    }
}
