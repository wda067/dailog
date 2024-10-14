package com.dailog.api.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import com.dailog.api.domain.PostEditor.PostEditorBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private long views;

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Builder
    public Post(String title, String content, Member member, List<Comment> comments, List<Likes> likes) {
        this.title = title;
        this.content = content;
        this.member = member;
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments = comments;
        this.likes = likes;
    }

    public PostEditorBuilder toEditor() {
        return PostEditor.builder()
                .title(title)
                .content(content);
    }

    public void edit(PostEditor postEditor) {
        title = postEditor.getTitle();
        content = postEditor.getContent();
    }

    public Long getMemberId() {
        return this.member.getId();
    }

    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }

    public void increaseViews() {
        this.views += 1;
    }

    public void updateViews(Integer views) {
        this.views = views;
    }

    public void addLikes(Likes likes) {
        this.likes.add(likes);
    }

    public void cancelLikes(Likes likes) {
        this.likes.remove(likes);
    }
}
