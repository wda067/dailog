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
import com.dailog.api.request.comment.CommentPageRequest;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.comment.CommentResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

        Comment comment;

        String parentCommentId = request.getParentId();
        //부모 댓글일 경우
        if (parentCommentId == null) {
            comment = Comment.builder()
                    .post(post)
                    .member(member)
                    .content(request.getContent())
                    .build();
        }
        //대댓글일 경우
        else {
            Comment parentComment = commentRepository.findById(Long.parseLong(parentCommentId))
                    .orElseThrow(CommentNotFound::new);
            comment = Comment.builder()
                    .post(post)
                    .member(member)
                    .content(request.getContent())
                    .parentComment(parentComment)
                    .build();
            //parentComment.addChildComment(comment);
        }

        member.addComment(comment);
        post.addComment(comment);
    }

    @Transactional
    public void writeByAnonymous(Long postId, CommentCreateForAnonymous request, String ipAddress) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);
        Comment comment;

        String parentCommentId = request.getParentId();
        //부모 댓글일 경우
        if (parentCommentId == null) {
            comment = Comment.builder()
                    .post(post)
                    .anonymousName(request.getAnonymousName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .content(request.getContent())
                    .ipAddress(ipAddress)
                    .build();
        }
        //대댓글일 경우
        else {
            Comment parentComment = commentRepository.findById(Long.parseLong(parentCommentId))
                    .orElseThrow(CommentNotFound::new);
            comment = Comment.builder()
                    .post(post)
                    .anonymousName(request.getAnonymousName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .content(request.getContent())
                    .ipAddress(ipAddress)
                    .parentComment(parentComment)
                    .build();
            //parentComment.addChildComment(comment);
        }

        post.addComment(comment);
    }

    public PagingResponse<CommentResponse> getList(Long postId, CommentPageRequest commentPageRequest) {
        Page<Comment> commentPage = commentRepository.getList(postId, commentPageRequest);

        return new PagingResponse<>(commentPage, CommentResponse.class);
    }

    public List<CommentResponse> get(Long postId) {
        List<Comment> parentComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        return parentComments.stream()
                .map(this::convertToDto)
                .toList();
    }

    //부모 댓글 DTO에 대댓글 DTO 리스트 추가
    private CommentResponse convertToDto(Comment parentComment) {
        List<CommentResponse> childComments = parentComment.getChildComments().stream()
                .map(this::convertChildCommentToDto)
                .toList();

        if (parentComment.getMember() == null) {
            //익명 댓글
            return CommentResponse.builder()
                    .id(parentComment.getId())
                    .anonymousName(parentComment.getAnonymousName())
                    .password(parentComment.getPassword())
                    .content(parentComment.getContent())
                    .createdAt(parentComment.getCreatedAt())
                    .updatedAt(parentComment.getUpdatedAt())
                    .ipAddress(parentComment.getIpAddress())
                    .childComments(childComments)  //자식 댓글 포함
                    .build();
        }

        //회원 댓글
        return CommentResponse.builder()
                .id(parentComment.getId())
                .memberId(parentComment.getMemberId())
                .nickname(parentComment.getMemberNickname())
                .content(parentComment.getContent())
                .createdAt(parentComment.getCreatedAt())
                .updatedAt(parentComment.getUpdatedAt())
                .childComments(childComments)  //자식 댓글 포함
                .build();
    }

    //대댓글 DTO 변환
    private CommentResponse convertChildCommentToDto(Comment childComment) {
        //익명 댓글
        if (childComment.getMember() == null) {
            return CommentResponse.builder()
                    .id(childComment.getId())
                    .anonymousName(childComment.getAnonymousName())
                    .password(childComment.getPassword())
                    .content(childComment.getContent())
                    .createdAt(childComment.getCreatedAt())
                    .updatedAt(childComment.getUpdatedAt())
                    .ipAddress(childComment.getIpAddress())
                    .build();
        }

        //회원 댓글
        return CommentResponse.builder()
                .id(childComment.getId())
                .memberId(childComment.getMemberId())
                .nickname(childComment.getMemberNickname())
                .content(childComment.getContent())
                .createdAt(childComment.getCreatedAt())
                .updatedAt(childComment.getUpdatedAt())
                .build();
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
