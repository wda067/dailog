package com.dailog.api.controller;

import static com.dailog.api.domain.enums.Role.MEMBER;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dailog.api.config.CustomMockAdmin;
import com.dailog.api.config.CustomMockMember;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.request.Join;
import com.dailog.api.request.Leave;
import com.dailog.api.request.member.MemberChangeNickname;
import com.dailog.api.request.member.MemberChangePassword;
import com.dailog.api.request.member.MemberResetPassword;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clean() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("계정 생성 성공")
    void createAccount_Success() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("테스트 이름")
                .nickname("테스트 별명")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        Member findMember = memberRepository.findByEmail(join.getEmail())
                .orElseThrow(MemberNotFound::new);

        assertEquals("test@test.com", findMember.getEmail());
        assertTrue(passwordEncoder.matches("ValidPassword12!", findMember.getPassword()));
        assertEquals("테스트 이름", findMember.getName());
    }

    @Test
    @DisplayName("계정 생성 실패 - 이메일 형식이 잘못됨")
    void createAccount_Fail_InvalidEmailFormat() throws Exception {
        //given
        Join join = Join.builder()
                .email("test")
                .password("ValidPassword12!")
                .name("테스트 이름")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.email").value("이메일 형식으로 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("계정 생성 실패 - 비밀번호 형식이 잘못됨")
    void createAccount_Fail_InvalidPasswordFormat() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("invalidPassword")
                .name("테스트 이름")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.password")
                        .value("유효하지 않은 비밀번호입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("계정 생성 실패 - 이름 공백 입력")
    void createAccount_Fail_EmptyName() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("")
                .nickname("테스트 별명")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("이름을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("계정 생성 실패 - 이름 8자 이상 입력")
    void createAccount_Fail_NameTooLong() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("8자이상테스트이름")
                .nickname("테스트 별명")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("이름은 8자까지 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("계정 생성 실패 - 별명 공백 입력")
    void createAccount_Fail_EmptyNickname() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("테스트 이름")
                .nickname("")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.nickname").value("별명을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("계정 생성 실패 - 별명 8자 이상 입력")
    void createAccount_Fail_NicknameTooLong() throws Exception {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("테스트 이름")
                .nickname("8자이상테스트별명")
                .build();
        String json = objectMapper.writeValueAsString(join);

        //when
        mockMvc.perform(post("/api/auth/join")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.nickname").value("별명은 8자까지 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member joinMember = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .role(MEMBER)
                .build();

        memberRepository.save(joinMember);

        HashMap<String, String> login = new HashMap<>();
        login.put("email", "test@test.com");
        login.put("password", "ValidPassword12!");

        String json = objectMapper.writeValueAsString(login);

        //expected
        mockMvc.perform(post("/api/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 미입력")
    void login_Fail_EmptyPassword() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member joinMember = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .build();

        memberRepository.save(joinMember);

        HashMap<String, String> login = new HashMap<>();
        login.put("email", "test@test.com");
        login.put("password", "");

        String json = objectMapper.writeValueAsString(login);

        //expected
        mockMvc.perform(post("/api/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호를 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 미입력")
    void login_Fail_EmptyEmail() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member joinMember = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .build();

        memberRepository.save(joinMember);

        HashMap<String, String> login = new HashMap<>();
        login.put("email", "");
        login.put("password", "ValidPassword12!");

        String json = objectMapper.writeValueAsString(login);

        //expected
        mockMvc.perform(post("/api/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 이메일 형식 입력")
    void login_Fail_InvalidEmailFormat() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member joinMember = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .build();

        memberRepository.save(joinMember);

        HashMap<String, String> login = new HashMap<>();
        login.put("email", "test");
        login.put("password", "ValidPassword12!");

        String json = objectMapper.writeValueAsString(login);

        //expected
        mockMvc.perform(post("/api/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 형식으로 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 입력")
    void login_Fail_IncorrectPassword() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member joinMember = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .build();

        memberRepository.save(joinMember);

        HashMap<String, String> login = new HashMap<>();
        login.put("email", "test@test.com");
        login.put("password", "InvalidPassword12!");

        String json = objectMapper.writeValueAsString(login);

        //expected
        mockMvc.perform(post("/api/auth/login")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 올바르지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    @CustomMockMember
    void leave_Success() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        Leave leave = new Leave("ValidPassword12!");
        String json = objectMapper.writeValueAsString(leave);

        //expected
        mockMvc.perform(post("/api/auth/{memberId}/leave", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 틀린 비밀번호")
    @CustomMockMember
    void leave_Fail_WrongPassword() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        Leave leave = new Leave("InvalidPassword12!");
        String json = objectMapper.writeValueAsString(leave);

        //expected
        mockMvc.perform(post("/api/auth/{memberId}/leave", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 올바르지 않습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 회원 삭제 성공")
    @CustomMockAdmin
    void delete_Success() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        //expected
        mockMvc.perform(post("/api/admin/{memberId}/delete", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 회원 삭제 실패 - 권한 없음")
    @CustomMockMember
    void delete_Fail_Forbidden() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        //expected
        mockMvc.perform(post("/api/admin/{memberId}/delete", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 정보 조회")
    @CustomMockMember
    void getLogInInfo() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        //expected
        mockMvc.perform(get("/api/member/me", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.role").value(member.getRole().name()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 조회")
    @CustomMockMember
    void getProfile() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //expected
        mockMvc.perform(get("/api/members/{memberId}", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value(member.getEmail()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.createdAt").value(member.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.role").value(member.getRole().name()))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 회원 정보 조회")
    @CustomMockAdmin
    void getProfileByAdmin_Success() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //expected
        mockMvc.perform(get("/api/admin/members/{memberId}", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value(member.getEmail()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.createdAt").value(member.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.role").value(member.getRole().name()))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 회원 정보 조회 실패 - 권한 없음")
    @CustomMockMember
    void getProfileByAdmin_Fail_Forbidden() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        //expected
        mockMvc.perform(get("/api/admin/members/{memberId}", member.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    @CustomMockMember
    void changePassword_Success() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("NewPassword12!")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 틀린 현재 비밀번호")
    @CustomMockMember
    void changePassword_Fail_WrongCurrentPassword() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("InvalidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("NewPassword12!")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 다른 확인 비밀번호")
    @CustomMockMember
    void changePassword_Fail_InvalidConfirm() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("InvalidPassword12!")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호와 일치")
    @CustomMockMember
    void changePassword_Fail_SamePassword() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("ValidPassword12!")
                .confirmPassword("ValidPassword12!")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 공백 입력")
    @CustomMockMember
    void changePassword_Fail_CurrentPasswordIsEmpty() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("")
                .newPassword("ValidPassword12!")
                .confirmPassword("ValidPassword12!")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.currentPassword").value("현재 비밀번호를 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유효하지 않은 비밀번호")
    @CustomMockMember
    void changePassword_Fail_InvalidPassword() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("1234")
                .confirmPassword("1234")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPassword").value("유효하지 않은 비밀번호입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 확인 비밀번호 공백 입력")
    @CustomMockMember
    void changePassword_Fail_ConfirmPasswordIsEmpty() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("")
                .build();

        String json = objectMapper.writeValueAsString(changePassword);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/password", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.confirmPassword").value("새로운 비밀번호를 한 번 더 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("별명 변경 성공")
    @CustomMockMember
    void changeNickname_Success() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangeNickname newNickname = new MemberChangeNickname("8자 별명");
        String json = objectMapper.writeValueAsString(newNickname);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/nickname", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("별명 변경 실패 - 8자 초과한 별명")
    @CustomMockMember
    void changeNickname_Fail_TooLongNickname() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangeNickname newNickname = new MemberChangeNickname("8자이상별명입니다");
        String json = objectMapper.writeValueAsString(newNickname);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/nickname", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newNickname").value("별명은 8자까지 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("별명 변경 실패 - 공백 입력")
    @CustomMockMember
    void changeNickname_Fail_EmptyInput() throws Exception {
        //given
        Member member = memberRepository.findAll().iterator().next();

        MemberChangeNickname newNickname = new MemberChangeNickname("");
        String json = objectMapper.writeValueAsString(newNickname);

        //expected
        mockMvc.perform(patch("/api/members/{memberId}/nickname", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newNickname").value("변경할 별명을 입력해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 조회")
    @CustomMockAdmin
    void getMemberList() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        List<Member> members = IntStream.range(1, 11)
                .mapToObj(i -> Member.builder()
                        .email("test" + i + "@test.com")
                        .password(encryptedPassword)
                        .name("testName" + i)
                        .nickname("testNickname" + i)
                        .role(Role.MEMBER)
                        .build())
                .toList();
        memberRepository.saveAll(members);

        //expected
        mockMvc.perform(get("/api/admin/members?page=1&size=10&")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(10)))
                .andExpect(jsonPath("$.items.[0].email").value("test10@test.com"))
                .andExpect(jsonPath("$.items.[0].name").value("testName10"))
                .andExpect(jsonPath("$.items.[0].nickname").value("testNickname10"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 조회 실패 - 권한 없음")
    @CustomMockMember
    void getMemberList_Fail_Forbidden() throws Exception {
        //expected
        mockMvc.perform(get("/api/admin/members?page=1&size=10&")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근할 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 검색 결과 조회")
    @CustomMockAdmin
    void getSearchResult() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        List<Member> members = IntStream.range(1, 11)
                .mapToObj(i -> Member.builder()
                        .email("test" + i + "@test.com")
                        .password(encryptedPassword)
                        .name("testName" + i)
                        .nickname("testNickname" + i)
                        .role(Role.MEMBER)
                        .build())
                .toList();
        memberRepository.saveAll(members);
        String searchQuery = "testName1";

        //expected
        mockMvc.perform(get("/api/admin/members?page=1&size=10&"
                        + "searchDateType=&searchType=name&searchQuery={searchQuery}", searchQuery)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.[0].email").value("test10@test.com"))
                .andExpect(jsonPath("$.items.[0].name").value("testName10"))
                .andExpect(jsonPath("$.items.[0].nickname").value("testNickname10"))
                .andExpect(jsonPath("$.items.[1].email").value("test1@test.com"))
                .andExpect(jsonPath("$.items.[1].name").value("testName1"))
                .andExpect(jsonPath("$.items.[1].nickname").value("testNickname1"))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 비밀번호 초기화")
    @CustomMockAdmin
    void initPassword() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        MemberResetPassword resetPassword = new MemberResetPassword("1234");
        String json = objectMapper.writeValueAsString(resetPassword);

        //expected
        mockMvc.perform(patch("/api/admin/members/{memberId}", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 비밀번호 실패 - 비밀번호 길이")
    @CustomMockAdmin
    void initPassword_Fail_InvalidPassword() throws Exception {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("test")
                .nickname("test")
                .role(MEMBER)
                .build();
        memberRepository.save(member);

        MemberResetPassword resetPassword = new MemberResetPassword("pw");
        String json = objectMapper.writeValueAsString(resetPassword);

        //expected
        mockMvc.perform(patch("/api/admin/members/{memberId}", member.getId())
                        .content(json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.password").value("비밀번호는 4자~16자로 입력해 주세요."))
                .andDo(print());
    }
}