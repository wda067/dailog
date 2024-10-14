package com.dailog.api.controller;

import static com.dailog.api.domain.enums.Role.MEMBER;
import static java.time.LocalDateTime.now;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dailog.api.config.CustomMockAdmin;
import com.dailog.api.config.CustomMockMember;
import com.dailog.api.domain.Likes;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.exception.post.PostNotFound;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.post.PostCreate;
import com.dailog.api.request.post.PostEdit;
import com.dailog.api.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc  //MockMvc 빈 주입
@SpringBootTest
class PostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LikesRepository likesRepository;

    @AfterEach
    void clean() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("게시글 작성 실패 - 로그인 하지 않음")
    void should_ReturnUnauthorized_When_NotLoggedIn() throws Exception {
        //given
        PostCreate request = PostCreate.builder()
                .title("123")
                .content("123")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("로그인 해주세요."))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 작성 요청시 title 값은 필수다.")
    void should_ReturnBadRequest_When_TitleIsMissing() throws Exception {
        //given
        PostCreate request = PostCreate.builder()
                .content("내용입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.title").value("제목을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 작성 요청시 content 값은 필수다.")
    void should_ReturnBadRequest_When_ContentIsMissing() throws Exception {
        //given
        PostCreate request = PostCreate.builder()
                .title("제목입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //expected
        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.content").value("내용을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 작성")
    void should_SavePost_When_LoggedIn() throws Exception {
        //given
        PostCreate request = PostCreate.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .build();
        String json = objectMapper.writeValueAsString(request);

        //when
        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        assertEquals(1L, postRepository.count());

        Post post = postRepository.findAll().get(0);
        assertEquals("제목입니다.", post.getTitle());
        assertEquals("내용입니다.", post.getContent());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 1개 조회")
    void should_GetOnePost_When_ValidRequest() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        Post post = Post.builder()
                .title("게시글 제목")
                .content("게시글 내용")
                .member(member)
                .build();
        postRepository.save(post);

        //expected
        mockMvc.perform(get("/api/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("게시글 제목"))
                .andExpect(jsonPath("$.content").value("게시글 내용"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 목록 조회")
    void should_GetPosts_When_ValidRequest() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(requestPosts);

        //expected
        String pagination = "page=1&size=10&sortByLikes=false";
        String searchParams = "searchDateType=&searchType=titleOrContent&searchQuery=";

        mockMvc.perform(get("/api/posts?" + pagination + "&" + searchParams)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(10)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 30"))
                .andExpect(jsonPath("$.items.[0].content").value("내용 30"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("페이지를 0으로 요청하면 첫 페이지를 가져온다.")
    void should_GetFirstPage_When_RequestZeroPage() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(requestPosts);

        //expected
        String pagination = "page=1&size=10&sortByLikes=false";
        String searchParams = "searchDateType=&searchType=titleOrContent&searchQuery=";

        mockMvc.perform(get("/api/posts?" + pagination + "&" + searchParams)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(10)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 30"))
                .andExpect(jsonPath("$.items[0].content").value("내용 30"))
                .andDo(print());
    }

    @Test
    @CustomMockAdmin
    @DisplayName("게시글 내용 수정")
    void should_EditPostContent_When_ValidRequest() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();

        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("제목 수정 전")
                .content("내용 수정 후")
                .build();

        //expected
        mockMvc.perform(patch("/api/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @CustomMockAdmin
    @DisplayName("게시글 내용 수정 실패 - 권한 없음")
    void should_FailToEdit_When_NotPostAuthor() throws Exception {
        //given
        Member newMember = Member.builder()
                .email("new@test.com")
                .name("new")
                .nickname("new")
                .role(MEMBER)
                .build();
        memberRepository.save(newMember);

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(newMember)
                .build();
        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("제목 수정 전")
                .content("내용 수정 후")
                .build();

        //expected
        mockMvc.perform(patch("/api/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("이 게시글의 작성자가 아닙니다."))
                .andDo(print());
    }

    @Test
    @CustomMockAdmin
    @DisplayName("게시글 삭제")
    void should_DeletePost_When_ValidRequest() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(member)
                .build();
        postRepository.save(post);

        //expected
        mockMvc.perform(delete("/api/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @CustomMockAdmin
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void should_FailToDelete_When_NotPostAuthor() throws Exception {
        //given
        Member newMember = Member.builder()
                .email("new@test.com")
                .name("new")
                .nickname("new")
                .role(MEMBER)
                .build();
        memberRepository.save(newMember);

        Post post = Post.builder()
                .title("제목 수정 전")
                .content("내용 수정 전")
                .member(newMember)
                .build();
        postRepository.save(post);

        //expected
        mockMvc.perform(delete("/api/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("이 게시글의 작성자가 아닙니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void should_ReturnNotFound_When_NotExistsPost() throws Exception {
        //expected
        mockMvc.perform(get("/api/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("존재하지 않는 게시글 수정")
    void should_FailToEdit_When_NotExistsPost() throws Exception {
        //given
        PostEdit postEdit = PostEdit.builder()
                .title("제목 수정")
                .content("내용 수정")
                .build();

        //expected
        mockMvc.perform(patch("/api/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시물 제목으로 검색")
    void should_ReturnPosts_When_SearchByTitle() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(requestPosts);

        String pageRequest = "page=1&size=30&sortByLikes=false";
        String searchRequest = "&searchDateType=&searchType=title&searchQuery=제목 1";
        //1, 10~19 -> 11개

        //expected
        mockMvc.perform(get("/api/posts?" + pageRequest + searchRequest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(11)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 19"))
                .andExpect(jsonPath("$.items.[0].content").value("내용 19"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시물 내용으로 검색")
    void should_ReturnPosts_When_SearchByContent() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(requestPosts);

        String pageRequest = "page=1&size=30&sortByLikes=false";
        String searchRequest = "&searchDateType=&searchType=content&searchQuery=내용 3";
        //3, 30 -> 2개

        //expected
        mockMvc.perform(get("/api/posts?" + pageRequest + searchRequest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 30"))
                .andExpect(jsonPath("$.items.[0].content").value("내용 30"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시물 제목 또는 내용으로 검색")
    void should_ReturnPosts_When_SearchByTitleOrContent() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();

        postRepository.saveAll(requestPosts);

        String pageRequest = "page=1&size=30&sortByLikes=false";
        String searchRequest = "&searchDateType=&searchType=titleOrContent&searchQuery=2";
        //2, 12, 20~29 -> 12개

        //expected
        mockMvc.perform(get("/api/posts?" + pageRequest + searchRequest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(12)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 29"))
                .andExpect(jsonPath("$.items.[0].content").value("내용 29"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 일주일 전까지 조회")
    void should_ReturnPost_When_SearchWithinOneWeek() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

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

        String updateQuery = "UPDATE post SET created_at = ? WHERE post_id = ?";
        jdbcTemplate.update(updateQuery, now().minusDays(1), oneDayAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusMonths(1), oneMonthAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusYears(1), oneYearAgoPost.getId());

        String pageRequest = "page=1&size=10&sortByLikes=false";
        String searchDateType = "1w";
        String searchRequest = "&searchDateType=" + searchDateType + "&searchType=&searchQuery=";
        //expected
        mockMvc.perform(get("/api/posts?" + pageRequest + searchRequest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items.[0].title").value("1일 전 제목"))
                .andExpect(jsonPath("$.items.[0].content").value("1일 전 내용"))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 6달 전까지 조회")
    void should_ReturnPost_When_SearchWithinAHalfYear() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

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

        String updateQuery = "UPDATE post SET created_at = ? WHERE post_id = ?";
        jdbcTemplate.update(updateQuery, now().minusDays(1), oneDayAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusMonths(1), oneMonthAgoPost.getId());
        jdbcTemplate.update(updateQuery, now().minusYears(1), oneYearAgoPost.getId());

        String pageRequest = "page=1&size=10&sortByLikes=false";
        String searchDateType = "6m";
        String searchRequest = "&searchDateType=" + searchDateType + "&searchType=&searchQuery=";

        //expected
        mockMvc.perform(get("/api/posts?" + pageRequest + searchRequest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.[0].title").value("1달 전 제목"))
                .andExpect(jsonPath("$.items.[1].title").value("1일 전 제목"))
                .andDo(print());
    }

    @Test
    @DisplayName("이전 게시글 조회")
    void should_ReturnPreviousPost_When_RequestingPreviousPostById() throws Exception {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title(i + "번째 게시글")
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //expected
        mockMvc.perform(get("/api/posts/{postId}/prev", posts.get(9).getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(posts.get(8).getId().intValue())))
                .andDo(print());
    }

    @Test
    @DisplayName("다음 게시글 조회")
    void should_ReturnNextPost_When_RequestingNextPostById() throws Exception {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title(i + "번째 게시글")
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //expected
        mockMvc.perform(get("/api/posts/{postId}/next", posts.get(0).getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(posts.get(1).getId().intValue())))
                .andDo(print());
    }

    @Test
    @DisplayName("이전 게시글이 존재하지 않는다.")
    void should_ReturnNotFound_When_PreviousPostDoesNotExist() throws Exception {
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
        postRepository.delete(first);

        //expected
        mockMvc.perform(get("/api/posts/{postId}/prev", second.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("다음 게시글이 존재하지 않는다.")
    void should_ReturnNotFound_When_NextPostDoesNotExist() throws Exception {
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
        postRepository.delete(second);

        //expected
        mockMvc.perform(get("/api/posts/{postId}/next", first.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("이전 게시물이 삭제가 되면 그 이전 게시물을 조회")
    void should_ReturnCorrectPost_When_PreviousPostIsDeleted() throws Exception {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title(i + "번째 게시글")
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        postRepository.delete(posts.get(8));

        //then
        mockMvc.perform(get("/api/posts/{postId}/prev", posts.get(9).getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(posts.get(7).getId().intValue())))
                .andDo(print());
    }

    @Test
    @DisplayName("다음 게시물이 삭제가 되면 그 다음 게시물을 조회")
    void should_ReturnCorrectPost_When_NextPostIsDeleted() throws Exception {
        //given
        List<Post> posts = IntStream.range(1, 11)
                .mapToObj(i -> Post.builder()
                        .title(i + "번째 게시글")
                        .content("내용")
                        .build())
                .toList();

        postRepository.saveAll(posts);

        //when
        postRepository.delete(posts.get(1));

        //then
        mockMvc.perform(get("/api/posts/{postId}/next", posts.get(0).getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(posts.get(2).getId().intValue())))
                .andDo(print());
    }

    @Test
    @CustomMockMember
    @DisplayName("1000명의 다른 사용자가 동시에 게시글을 조회한다.")
    void should_IncreaseViews_When_1000UsersViewPostSimultaneously() throws Exception {
        //given
        PostCreate postCreate = PostCreate.builder()
                .title("제목")
                .content("내용")
                .build();
        String json = objectMapper.writeValueAsString(postCreate);
        //게시글 작성 API 요청
        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        int userCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);  //1000개의 요청을 동시에 시작하도록 제어하는 래치 설정

        Long postId = postRepository.findAll().get(0).getId();

        //when
        for (int i = 0; i < userCount; i++) {
            int userId = i;
            executorService.execute(() -> {
                try {
                    //게시글 조회 API 요청
                    mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId)
                                    .remoteAddress("test.ip." + userId)
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();  //요청 완료 시 카운트 감소
                }
            });
        }

        latch.await();  //모든 요청이 완료될 때까지 대기

        //then
        assertEquals(1L, postRepository.count());

        Post findPost = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);
        Integer views = (Integer) redisTemplate.opsForValue().get("post:views:" + findPost.getId());
        assertEquals(userCount, views);
    }

    @Test
    @DisplayName("관리자는 모든 게시글을 삭제할 수 있다.")
    @CustomMockAdmin
    void should_DeletePost_When_Admin() throws Exception {
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

        //expected
        mockMvc.perform(delete("/api/admin/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        assertEquals(0, postRepository.count());
    }

    @Test
    @DisplayName("관리자가 아니라면 다른 게시글을 삭제할 수 없다.")
    @CustomMockMember
    void should_NotDeletePost_When_NotAdmin() throws Exception {
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

        //expected
        mockMvc.perform(delete("/api/admin/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."))
                .andDo(print());

        assertEquals(1, postRepository.count());
    }

    @Test
    @CustomMockMember
    @DisplayName("게시글 목록 좋아요 순으로 조회")
    void should_GetPosts_When_SortedByLikes() throws Exception {
        //given
        Member member = memberRepository.findAll().get(0);

        List<Post> requestPosts = IntStream.range(1, 31)
                .mapToObj(i -> Post.builder()
                        .title("제목 " + i)
                        .content("내용 " + i)
                        .member(member)
                        .build())
                .toList();
        postRepository.saveAll(requestPosts);

        Likes likes = Likes.builder()
                .member(member)
                .post(requestPosts.get(0))
                .build();
        likesRepository.save(likes);

        //expected
        String pagination = "page=1&size=10&sortByLikes=true";
        String searchParams = "searchDateType=&searchType=titleOrContent&searchQuery=";

        mockMvc.perform(get("/api/posts?" + pagination + "&" + searchParams)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(10)))
                .andExpect(jsonPath("$.items.[0].title").value("제목 1"))
                .andExpect(jsonPath("$.items.[0].content").value("내용 1"))
                .andExpect(jsonPath("$.items.[0].likes").value("1"))
                .andDo(print());
    }
}