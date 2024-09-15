package com.dailog.api.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dailog.api.config.CustomMockAdmin;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.post.PostCreate;
import com.dailog.api.request.post.PostEdit;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.dailog.com", uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
public class PostControllerDocTest {

    //@Autowired
    //private MockMvc mockMvc;
    //
    //@Autowired
    //private PostRepository postRepository;
    //
    //@Autowired
    //private ObjectMapper objectMapper;
    //
    //@Autowired
    //private MemberRepository memberRepository;
    //
    //@AfterEach
    //void clean() {
    //    postRepository.deleteAll();
    //    memberRepository.deleteAll();
    //}
    //
    //@Test
    //@DisplayName("게시글 단건 조회")
    //void getPostById() throws Exception {
    //    //given
    //    Member member = Member.builder()
    //            .name("author")
    //            .email("test@test.com")
    //            .password("ValidPassword12!")
    //            .role(Role.MEMBER)
    //            .build();
    //    memberRepository.save(member);
    //
    //    Post post = Post.builder()
    //            .title("제목")
    //            .content("내용")
    //            .member(member)
    //            .build();
    //    postRepository.save(post);
    //
    //    //expected
    //    mockMvc.perform(get("/api/posts/{postId}", post.getId())
    //                    .accept(APPLICATION_JSON))
    //            .andExpect(status().isOk())
    //            .andDo(print())
    //            .andDo(document("post-inquiry", pathParameters(
    //                            parameterWithName("postId").description("게시글 ID")
    //                    ),
    //                    responseFields(
    //                            fieldWithPath("id").description("게시글 ID"),
    //                            fieldWithPath("title").description("게시글 제목"),
    //                            fieldWithPath("content").description("게시글 내용"),
    //                            fieldWithPath("createdAt").description("게시글 생성일"),
    //                            fieldWithPath("updatedAt").description("게시글 수정일"),
    //                            fieldWithPath("memberId").description("게시글 작성자 ID")
    //                    )
    //            ));
    //}
    //
    //@Test
    //@CustomMockAdmin
    //@DisplayName("게시글 등록")
    //void createPost() throws Exception {
    //    //given
    //    PostCreate request = PostCreate.builder()
    //            .title("제목")
    //            .content("내용")
    //            .build();
    //
    //    String json = objectMapper.writeValueAsString(request);
    //
    //    //expected
    //    mockMvc.perform(post("/api/posts")
    //                    .contentType(APPLICATION_JSON)
    //                    .content(json)
    //                    .header("authorization", "user"))
    //            .andExpect(status().isOk())
    //            .andDo(print())
    //            .andDo(document("post-create",
    //                    requestFields(
    //                            fieldWithPath("title").description("게시글 제목")
    //                                    .attributes(key("constraint").value("좋은 제목 입력")),
    //                            fieldWithPath("content").description("게시글 내용").optional()
    //                    )
    //            ));
    //}
    //
    //@Test
    //@WithMockUser(roles = {"ADMIN"})
    //@DisplayName("게시글 수정")
    //void editPost() throws Exception {
    //    // given
    //    Post post = Post.builder()
    //            .title("제목 수정 전")
    //            .content("내용 수정 전")
    //            .build();
    //    postRepository.save(post);
    //
    //    PostEdit postEdit = PostEdit.builder()
    //            .title("제목 수정 후")
    //            .content("내용 수정 후")
    //            .build();
    //    String json = objectMapper.writeValueAsString(postEdit);
    //
    //    // expected
    //    mockMvc.perform(patch("/api/posts/{postId}", post.getId())
    //                    .contentType(APPLICATION_JSON)
    //                    .content(json)
    //                    .header("authorization", "user"))
    //            .andExpect(status().isOk())
    //            .andDo(print())
    //            .andDo(document("post-edit",
    //                    pathParameters(
    //                            parameterWithName("postId").description("게시글 ID")
    //                    ),
    //                    requestFields(
    //                            fieldWithPath("title").description("수정할 제목"),
    //                            fieldWithPath("content").description("수정할 내용")
    //                    )
    //            ));
    //}
    //
    //@Test
    //@CustomMockAdmin
    //@DisplayName("게시글 삭제")
    //void deletePost() throws Exception {
    //    // given
    //    Member member = memberRepository.findAll().get(0);
    //
    //    Post post = Post.builder()
    //            .title("제목")
    //            .content("내용")
    //            .member(member)
    //            .build();
    //    postRepository.save(post);
    //
    //    // expected
    //    mockMvc.perform(delete("/api/posts/{postId}", post.getId())
    //                    .contentType(APPLICATION_JSON)
    //                    .header("authorization", "user"))
    //            .andExpect(status().isOk())
    //            .andDo(document("post-delete",
    //                    pathParameters(
    //                            parameterWithName("postId").description("게시글 ID")
    //                    )
    //            ));
    //}
    //
    //@Test
    //@DisplayName("게시글 조회")
    //void getPost() throws Exception {
    //    // given
    //    Member member = Member.builder()
    //            .name("author")
    //            .email("test@test.com")
    //            .password("ValidPassword12!")
    //            .role(Role.MEMBER)
    //            .build();
    //    memberRepository.save(member);
    //
    //    Post post = Post.builder()
    //            .title("제목")
    //            .content("내용")
    //            .member(member)
    //            .build();
    //    postRepository.save(post);
    //
    //    // expected
    //    mockMvc.perform(get("/api/posts/{postId}", post.getId())
    //                    .contentType(APPLICATION_JSON))
    //            .andExpect(status().isOk())
    //            .andDo(document("post-get",
    //                    preprocessRequest(prettyPrint()),
    //                    preprocessResponse(prettyPrint()),
    //                    pathParameters(
    //                            parameterWithName("postId").description("게시글 ID")
    //                    ),
    //                    responseFields(
    //                            fieldWithPath("id").description("게시글 ID"),
    //                            fieldWithPath("title").description("제목"),
    //                            fieldWithPath("content").description("내용"),
    //                            fieldWithPath("createdAt").description("게시글 생성일"),
    //                            fieldWithPath("updatedAt").description("게시글 수정일"),
    //                            fieldWithPath("memberId").description("게시글 작성자 ID")
    //                    )
    //            ));
    //}
    //
    //@Test
    //@DisplayName("게시글 목록 조회")
    //void getPosts() throws Exception {
    //    // given
    //    Member member = Member.builder()
    //            .name("author")
    //            .email("test@test.com")
    //            .password("ValidPassword12!")
    //            .role(Role.MEMBER)
    //            .build();
    //    memberRepository.save(member);
    //
    //    List<Post> requestPosts = IntStream.range(1, 31)
    //            .mapToObj(i -> Post.builder()
    //                    .title("제목 " + i)
    //                    .content("내용 " + i)
    //                    .member(member)
    //                    .build())
    //            .toList();
    //    postRepository.saveAll(requestPosts);
    //
    //    // expected
    //    mockMvc.perform(get("/api/posts")
    //                    .param("page", "1")
    //                    .param("size", "5")
    //                    .contentType(APPLICATION_JSON))
    //            .andExpect(status().isOk())
    //            .andDo(print())
    //            .andDo(document("post-list",
    //                    preprocessRequest(prettyPrint()),
    //                    preprocessResponse(prettyPrint()),
    //                    queryParameters(
    //                            parameterWithName("page").description("페이지 번호"),
    //                            parameterWithName("size").description("페이지 크기")
    //                    ),
    //                    responseFields(
    //                            fieldWithPath("items[].id").description("게시글 ID"),
    //                            fieldWithPath("items[].title").description("제목"),
    //                            fieldWithPath("items[].content").description("내용"),
    //                            fieldWithPath("items[].createdAt").description("게시글 생성일"),
    //                            fieldWithPath("items[].updatedAt").description("게시글 수정일"),
    //                            fieldWithPath("items[].memberId").description("게시글 작성자 ID"),
    //                            fieldWithPath("page").description("현재 페이지 번호"),
    //                            fieldWithPath("size").description("페이지 크기"),
    //                            fieldWithPath("totalCount").description("전체 게시글 수")
    //                    )
    //            ));
    //}

}
