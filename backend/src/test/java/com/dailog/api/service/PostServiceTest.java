package com.dailog.api.service;

import static com.dailog.api.domain.enums.Role.ADMIN;
import static com.dailog.api.domain.enums.Role.MEMBER;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dailog.api.domain.Comment;
import com.dailog.api.domain.Likes;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.post.ForbiddenPostAccess;
import com.dailog.api.exception.post.PostNotFound;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.comment.CommentRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.post.PostCreate;
import com.dailog.api.request.post.PostEdit;
import com.dailog.api.request.post.PostPageRequest;
import com.dailog.api.request.post.PostSearch;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.post.PostDetailResponse;
import com.dailog.api.response.post.PostIdResponse;
import com.dailog.api.response.post.PostListResponse;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PostServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LikesRepository likesRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void clean() {
        likesRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private Member getMember() {
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .name("member")
                .email("member@test.com")
                .password(encryptedPassword)
                .role(MEMBER)
                .build();
        memberRepository.save(member);
        return member;
    }

    private Member getAdmin() {
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .name("admin")
                .email("admin@test.com")
                .password(encryptedPassword)
                .role(ADMIN)
                .build();
        memberRepository.save(member);
        return member;
    }

    @Test
    @DisplayName("글 작성")
    void should_CreatePost_When_ValidRequest() {
        //given
        Member member = getMember();

        PostCreate postCreate = PostCreate.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        //when
        postService.write(postCreate, member.getEmail());

        //then
        assertEquals(1L, postRepository.count());
        Post post = postRepository.findAll().get(0);
        assertEquals("제목입니다.", post.getTitle());
        assertEquals("내용입니다.", post.getContent());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("글 1개 조회")
    void should_GetPost_When_ValidId() {
        //given
        Member member = getMember();

        Post post = Post.builder()
                .title("날씨가 좋아요.")
                .content("놀러가고 싶어요.")
                .member(member)
                .build();
        postRepository.save(post);

        //when
        PostDetailResponse savedPost = postService.get(post.getId());

        //then
        assertNotNull(savedPost);
        assertEquals("날씨가 좋아요.", post.getTitle());
        assertEquals("놀러가고 싶어요.", post.getContent());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("글 1페이지 조회")
    void should_GetPagedPosts_When_ValidPageRequest() {
        //given
        Member member = getMember();

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(requestPosts);

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        PostSearch postSearch = new PostSearch();

        //when
        PagingResponse<PostListResponse> pagingResponse = postService.getList(postSearch, postPageRequest);

        //then
        assertEquals(10L, pagingResponse.getSize());
        assertEquals("제목 30", pagingResponse.getItems().get(0).getTitle());
        assertEquals("제목 21", pagingResponse.getItems().get(9).getTitle());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("글 1페이지 조회 - 좋아요 순으로 정렬")
    void should_GetPagedPosts_When_SortByLikes() {
        //given
        Member member = getMember();

        List<Post> posts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(posts);

        IntStream.range(0, 30)
                        .forEach(i -> {
                            if (i % 2 != 0) {
                                Likes likes = Likes.builder()
                                        .member(member)
                                        .post(posts.get(i))
                                        .build();
                                likesRepository.save(likes);
                            }
                        });

        PostPageRequest postPageRequest = PostPageRequest.builder()
                .page(1)
                .size(10)
                .sortByLikes(true)
                .build();

        PostSearch postSearch = new PostSearch();

        //when
        PagingResponse<PostListResponse> pagingResponse = postService.getList(postSearch, postPageRequest);

        //then
        assertEquals(10L, pagingResponse.getSize());
        assertEquals("제목 30", pagingResponse.getItems().get(0).getTitle());
        assertEquals("제목 12", pagingResponse.getItems().get(9).getTitle());
        assertEquals(1L, pagingResponse.getItems().get(0).getLikes());
        assertEquals(1L, pagingResponse.getItems().get(9).getLikes());
    }

    @Test
    @DisplayName("글 제목 수정")
    void should_EditPostTitle_When_ValidEditRequest() {
        //given
        Member member = getMember();

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("제목 수정 후")
                .content(null)
                .build();

        //when
        postService.edit(post.getId(), postEdit, member.getEmail());

        //then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(PostNotFound::new);
        assertEquals("제목 수정 후", editedPost.getTitle());
        assertEquals("내용 수정 전", editedPost.getContent());
    }

    @Test
    @DisplayName("글 내용 수정")
    void should_EditPostContent_When_ValidEditRequest() {
        //given
        Member member = getMember();

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title(null)
                .content("내용 수정 후")
                .build();

        //when
        postService.edit(post.getId(), postEdit, member.getEmail());

        //then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id=" + post.getId()));
        assertEquals("내용 수정 후", editedPost.getContent());
        assertEquals("제목 수정 전", editedPost.getTitle());
    }

    @Test
    @DisplayName("게시글 삭제")
    void should_DeletePost_When_ValidId() {
        //given
        Member member = getMember();

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        //when
        postService.delete(post.getId(), member.getEmail());

        //then
        assertEquals(0, postRepository.count());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("글 1개 조회 실패 - 존재하지 않는 글")
    void should_ThrowPostNotFound_When_PostDoesNotExist() {
        //given
        Member member = getMember();

        Post post = Post.builder()
                .title("글 제목")
                .content("글 내용")
                .member(member)
                .build();
        postRepository.save(post);

        //expected
        assertThrows(PostNotFound.class, () ->
                postService.delete(post.getId() + 1L, member.getEmail()));
    }

    @Test
    @DisplayName("글 내용 수정 실패 - 존재하지 않는 글")
    void should_ThrowPostNotFound_When_EditNonexistentPost() {
        //given
        Member member = getMember();
        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title(null)
                .content("내용 수정 후")
                .build();

        //expected
        assertThrows(PostNotFound.class, () ->
                postService.edit(post.getId() + 1L, postEdit, member.getEmail()));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 글")
    void should_ThrowPostNotFound_When_DeleteNonexistentPost() {
        //given
        Member member = getMember();
        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        //expected
        assertThrows(PostNotFound.class, () ->
                postService.delete(post.getId() + 1L, member.getEmail()));
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("게시글 내용으로 검색")
    void should_SearchPostsByContent_When_ValidSearchQuery() {
        //given
        Member member = getMember();

        List<Post> requestPosts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(requestPosts);

        PostSearch postSearch = new PostSearch();
        postSearch.setSearchType("content");  //내용으로 검색
        postSearch.setSearchQuery("내용 10");  //"내용 10"이 포함된 게시글

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        //when
        PagingResponse<PostListResponse> response = postService.getList(postSearch, postPageRequest);

        //then
        assertEquals(1L, response.getItems().size());
        //assertEquals("내용 10", response.getItems().get(0).getContent());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("게시글 제목으로 검색")
    void should_SearchPostsByTitle_When_ValidSearchQuery() {
        //given
        Member member = getMember();

        List<Post> requestPosts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(requestPosts);

        PostSearch postSearch = new PostSearch();
        postSearch.setSearchType("title");  //제목으로 검색
        postSearch.setSearchQuery("제목");  //"제목"이 포함된 게시글

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        //when
        PagingResponse<PostListResponse> response = postService.getList(postSearch, postPageRequest);

        //then
        assertEquals(10L, response.getItems().size());
        assertEquals("제목 10", response.getItems().get(0).getTitle());
        assertEquals("제목 1", response.getItems().get(9).getTitle());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("게시글 제목 또는 내용으로 검색")
    void should_SearchPostsByTitleOrContent_When_ValidSearchQuery() {
        //given
        Member member = getMember();
        List<Post> requestPosts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(requestPosts);

        PostSearch postSearch = new PostSearch();
        postSearch.setSearchType("titleOrContent");  //제목 또는 내용으로 검색
        postSearch.setSearchQuery("제목 10");  //"제목 10"이 포함된 게시글

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        //when
        PagingResponse<PostListResponse> response = postService.getList(postSearch, postPageRequest);

        //then
        assertEquals(1L, response.getItems().size());
        assertEquals("제목 10", response.getItems().get(0).getTitle());
        //assertEquals("내용 10", response.getItems().get(0).getContent());
    }

    @Test
    @Transactional
    @DisplayName("게시글 일주일 전까지 조회")
    void should_GetPostsFromLastWeek_When_ValidRequest() {
        //given
        Member member = getMember();

        Post oneDayAgoPost = Post.builder()
                .title("1일 전 제목")
                .content("1일 전 내용")
                .member(member)
                .build();

        Post oneMonthAgoPost = Post.builder()
                .title("1달 전 제목")
                .content("1달 전 내용")
                .member(member)
                .build();

        Post oneYearAgoPost = Post.builder()
                .title("1년 전 제목")
                .content("1년 전 내용")
                .member(member)
                .build();

        postRepository.save(oneDayAgoPost);
        postRepository.save(oneMonthAgoPost);
        postRepository.save(oneYearAgoPost);

        //SQL을 사용하여 createdAt 필드 수정
        String updateQuery = "UPDATE post SET created_at = ? WHERE post_id = ?";
        jdbcTemplate.update(updateQuery, now().minusDays(1), oneDayAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusMonths(1), oneMonthAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusYears(1), oneYearAgoPost.getId());

        PostSearch sevenDaysAgo = new PostSearch();
        sevenDaysAgo.setSearchDateType("1w");

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        //when
        PagingResponse<PostListResponse> response = postService.getList(sevenDaysAgo, postPageRequest);

        //then
        assertEquals(1L, response.getItems().size());
        assertEquals("1일 전 제목", response.getItems().get(0).getTitle());
        //assertEquals("1일 전 내용", response.getItems().get(0).getContent());
    }

    @Test
    @Transactional
    @DisplayName("게시글 6달 전까지 조회")
    void should_GetPostsFromLastSixMonths_When_ValidRequest() {
        //given
        Member member = getMember();
        Post oneDayAgoPost = Post.builder()
                .title("1일 전 제목")
                .content("1일 전 내용")
                .member(member)
                .build();

        Post oneMonthAgoPost = Post.builder()
                .title("1달 전 제목")
                .content("1달 전 내용")
                .member(member)
                .build();

        Post oneYearAgoPost = Post.builder()
                .title("1년 전 제목")
                .content("1년 전 내용")
                .member(member)
                .build();

        postRepository.save(oneDayAgoPost);
        postRepository.save(oneMonthAgoPost);
        postRepository.save(oneYearAgoPost);

        //SQL을 사용하여 createdAt 필드 수정
        String updateQuery = "UPDATE post SET created_at = ? WHERE post_id = ?";
        jdbcTemplate.update(updateQuery, now().minusDays(1), oneDayAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusMonths(1), oneMonthAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusYears(1), oneYearAgoPost.getId());

        PostSearch sixMonthAgo = new PostSearch();
        sixMonthAgo.setSearchDateType("6m");

        PostPageRequest postPageRequest = PostPageRequest.builder().build();

        //when
        PagingResponse<PostListResponse> response = postService.getList(sixMonthAgo, postPageRequest);

        //then
        assertEquals(2L, response.getItems().size());
        assertEquals("1달 전 제목", response.getItems().get(0).getTitle());
        //assertEquals("1달 전 내용", response.getItems().get(0).getContent());
        assertEquals("1일 전 제목", response.getItems().get(1).getTitle());
        //assertEquals("1일 전 내용", response.getItems().get(1).getContent());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("이전 게시글 조회")
    void should_GetPreviousPost_When_ValidId() {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        PostIdResponse prevPostId = postService.getPrevPostId(posts.get(9).getId());

        //then
        Post post = postRepository.findById(prevPostId.getId())
                .orElseThrow(PostNotFound::new);
        assertEquals(posts.get(8).getId(), post.getId());
        assertEquals("제목 9", post.getTitle());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("다음 게시글 조회")
    void should_GetNextPost_When_ValidId() {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        PostIdResponse nextPostId = postService.getNextPostId(posts.get(0).getId());

        //then
        Post post = postRepository.findById(nextPostId.getId())
                .orElseThrow(PostNotFound::new);
        assertEquals(posts.get(1).getId(), post.getId());
        assertEquals("제목 2", post.getTitle());
    }

    @Test
    @Transactional
    @DisplayName("이전 게시글이 존재하지 않는다.")
    void should_ThrowPostNotFound_When_NoPreviousPostExists() {
        //given
        Post first = Post.builder()
                .title("1번째 게시글")
                .content("내용")
                .build();

        Post second = Post.builder()
                .title("2번째 게시글")
                .content("내용")
                .build();

        postRepository.save(first);
        postRepository.save(second);

        //expected
        assertThrows(PostNotFound.class, () ->
                postService.getPrevPostId(first.getId()));
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("다음 게시글이 존재하지 않는다.")
    void should_ThrowPostNotFound_When_NoNextPostExists() {
        //given
        Post first = Post.builder()
                .title("1번째 게시글")
                .content("내용")
                .build();

        Post second = Post.builder()
                .title("2번째 게시글")
                .content("내용")
                .build();

        postRepository.save(first);
        postRepository.save(second);

        //expected
        assertThrows(PostNotFound.class, () ->
                postService.getNextPostId(second.getId()));
    }

    @Test
    @DisplayName("이전 게시물이 삭제가 되면 그 이전 게시물을 조회")
    void should_GetPreviousPost_When_PrevPostIsDeleted() {
        //given
        Member member = getMember();
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용")
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        postService.delete(posts.get(8).getId(), member.getEmail());

        //then
        Post post = postRepository.findPrevPost(posts.get(9).getId())
                .orElseThrow(PostNotFound::new);
        assertEquals(posts.get(7).getId(), post.getId());
        assertEquals("제목 8", post.getTitle());
    }

    @Test
    @DisplayName("다음 게시물이 삭제가 되면 그 다음 게시물을 조회")
    void should_GetNextPost_When_NextPostIsDeleted() {
        //given
        Member member = getMember();
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용")
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        postService.delete(posts.get(1).getId(), member.getEmail());

        //then
        Post post = postRepository.findNextPost(posts.get(0).getId())
                .orElseThrow(PostNotFound::new);
        assertEquals(posts.get(2).getId(), post.getId());
        assertEquals("제목 3", post.getTitle());
    }

    @Test
    @DisplayName("게시글 삭제 시 댓글도 삭제된다.")
    void should_DeleteComments_When_DeletePost() {
        //given
        Member member = getMember();
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        postRepository.save(post);

        IntStream.range(0, 10)
                .forEach(i -> {
                            Comment comment = Comment.builder()
                                    .content("댓글")
                                    .post(post)
                                    .member(member)
                                    .build();
                            commentRepository.save(comment);
                        }
                );

        //when
        postService.delete(post.getId(), member.getEmail());

        //then
        List<Comment> comments = commentRepository.findAll();
        assertEquals(0, comments.size());
    }

    @Test
    @DisplayName("관리자는 모든 게시글을 삭제할 수 있다.")
    void should_DeletePost_When_Admin() {
        //given
        Member member = getMember();
        Member admin = getAdmin();

        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        postRepository.save(post);

        //when
        postService.deleteByAdmin(post.getId(), admin.getEmail());

        //then
        assertEquals(0, postRepository.count());
    }

    @Test
    @DisplayName("관리자가 아니라면 다른 게시글을 삭제할 수 없다.")
    void should_NotDeletePost_When_NotAdmin() {
        //given
        Member member = getMember();
        Member admin = getAdmin();

        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(admin)
                .build();
        postRepository.save(post);

        //expected
        assertThrows(ForbiddenPostAccess.class, () ->
                postService.deleteByAdmin(post.getId(), member.getEmail()));
        assertEquals(1L, postRepository.count());
    }
}