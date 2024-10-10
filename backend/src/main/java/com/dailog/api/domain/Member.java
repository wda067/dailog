package com.dailog.api.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.dailog.api.domain.MemberEditor.MemberEditorBuilder;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.response.oAuth2.OAuth2Response;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String nickname;

    private String email;

    private String password;

    @OneToMany(mappedBy = "member", cascade = ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(STRING)
    private Role role;

    private boolean oAuth2Login;

    @Builder
    public Member(String name, String nickname, String email, String password, Role role, boolean oAuth2Login) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.oAuth2Login = oAuth2Login;
    }

    public MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .password(password)
                .nickName(nickname);
    }

    public void edit(MemberEditor memberEditor) {
        password = memberEditor.getPassword();
        nickname = memberEditor.getNickName();
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}
