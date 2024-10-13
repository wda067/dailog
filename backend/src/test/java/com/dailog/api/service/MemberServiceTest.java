package com.dailog.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.exception.auth.EmailAlreadyExists;
import com.dailog.api.exception.auth.SamePassword;
import com.dailog.api.exception.member.InvalidMemberPassword;
import com.dailog.api.exception.member.InvalidNickname;
import com.dailog.api.exception.member.InvalidPasswordConfirm;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.request.Join;
import com.dailog.api.request.Leave;
import com.dailog.api.request.member.MemberChangeNickname;
import com.dailog.api.request.member.MemberChangePassword;
import com.dailog.api.request.member.MemberPageRequest;
import com.dailog.api.request.member.MemberResetPassword;
import com.dailog.api.request.member.MemberSearch;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.member.MemberProfile;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LikesRepository likesRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        likesRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void join_Success() {
        //given
        Join join = Join.builder()
                .email("test@test.com")
                .password("ValidPassword12!")
                .name("testName")
                .nickname("testNickname")
                .build();

        //when
        memberService.join(join);

        //then
        assertEquals(1L, memberRepository.count());

        Member member = memberRepository.findAll().iterator().next();

        assertEquals("test@test.com", member.getEmail());
        assertTrue(passwordEncoder.matches(join.getPassword(), member.getPassword()));
        assertEquals("testName", member.getName());
        assertEquals("testNickname", member.getNickname());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void join_Fail_EmailAlreadyExists() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        //expected
        assertThrows(EmailAlreadyExists.class, () ->
                memberService.join(Join.builder()
                        .email("test@test.com")
                        .password("ValidPassword12!")
                        .name("testName")
                        .nickname("testNickname")
                        .build()));
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void leave_Success() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        Leave leave = new Leave("ValidPassword12!");

        //when
        memberService.leave(member.getId(), leave, member.getEmail());

        //then
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        assertTrue(findMember.isEmpty());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 틀린 비밀번호")
    void leave_Fail_WrongPassword() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        Leave leave = new Leave("InvalidPassword");

        //expected
        assertThrows(InvalidMemberPassword.class, () -> memberService.leave(member.getId(), leave, member.getEmail()));
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void delete_Success() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        //when
        memberService.delete(member.getId());

        //then
        List<Member> members = memberRepository.findAll();
        assertEquals(0, members.size());
    }

    @Test
    @DisplayName("회원 삭제 - 존재하지 않는 회원")
    void delete_Fail_NotFound() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        //expected
        memberService.delete(member.getId());
        Assertions.assertThrows(MemberNotFound.class, () ->
                memberService.delete(member.getId()));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("NewPassword12!")
                .build();
        //when
        memberService.changePassword(member.getId(), changePassword, member.getEmail());

        //then
        assertTrue(passwordEncoder.matches(changePassword.getNewPassword(), member.getPassword()));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 변경 실패 - 틀린 현재 비밀번호")
    void changePassword_Fail_WrongCurrentPassword() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("InvalidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("NewPassword12!")
                .build();

        //expected
        assertThrows(InvalidMemberPassword.class, () ->
                memberService.changePassword(member.getId(), changePassword, member.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 변경 실패 - 다른 확인 비밀번호")
    void changePassword_Fail_InvalidConfirm() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("NewPassword12!")
                .confirmPassword("InvalidPassword12!")
                .build();

        //expected
        assertThrows(InvalidPasswordConfirm.class, () ->
                memberService.changePassword(member.getId(), changePassword, member.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호와 일치")
    void changePassword_Fail_SamePassword() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangePassword changePassword = MemberChangePassword.builder()
                .currentPassword("ValidPassword12!")
                .newPassword("ValidPassword12!")
                .confirmPassword("ValidPassword12!")
                .build();

        //expected
        assertThrows(SamePassword.class, () ->
                memberService.changePassword(member.getId(), changePassword, member.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("별명 변경 성공")
    void changeNickname_Success() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangeNickname newNickname = new MemberChangeNickname("newNickname");

        //when
        memberService.changeNickname(member.getId(), newNickname, member.getEmail());

        //then
        assertEquals(newNickname.getNewNickname(), member.getNickname());
    }

    @Test
    @Transactional
    @DisplayName("별명 변경 실패 - 동일한 별명")
    void changeNickname_Fail_SameNickname() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        MemberChangeNickname newNickname = new MemberChangeNickname("testNickname");

        //expected
        assertThrows(InvalidNickname.class, () ->
                memberService.changeNickname(member.getId(), newNickname, member.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("별명 변경 실패 - 존재하는 별명")
    void changeNickname_Fail_AlreadyExistsNickname() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);

        Member newMember = Member.builder()
                .email("test2@test.com")
                .password(encryptedPassword)
                .name("newName")
                .nickname("newNickname")
                .build();

        memberRepository.save(newMember);

        MemberChangeNickname newNickname = new MemberChangeNickname("testNickname");

        //expected
        assertThrows(InvalidNickname.class, () ->
                memberService.changeNickname(newMember.getId(), newNickname, newMember.getEmail()));
    }

    @Test
    @DisplayName("회원 1페이지 조회")
    void getMemberList_1Page() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        List<Member> members = IntStream.range(1, 6)
                .mapToObj(i -> Member.builder()
                        .email("test" + i + "@test.com")
                        .password(encryptedPassword)
                        .name("testName" + i)
                        .nickname("testNickname" + i)
                        .role(Role.MEMBER)
                        .build())
                .toList();
        memberRepository.saveAll(members);

        MemberPageRequest pageRequest = MemberPageRequest.builder()
                .page(1)
                .size(5)
                .build();

        //when
        PagingResponse<MemberProfile> pagingResponse = memberService.getList(pageRequest);

        //then
        assertEquals(5L, pagingResponse.getSize());
        assertEquals("test5@test.com", pagingResponse.getItems().get(0).getEmail());
    }

    @Test
    @DisplayName("회원 목록 검색")
    void getMemberList_SearchResult() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        List<Member> members = IntStream.range(1, 6)
                .mapToObj(i -> Member.builder()
                        .email("test" + i + "@test.com")
                        .password(encryptedPassword)
                        .name("testName" + i)
                        .nickname("testNickname" + i)
                        .role(Role.MEMBER)
                        .build())
                .toList();
        memberRepository.saveAll(members);

        MemberPageRequest pageRequest = MemberPageRequest.builder()
                .page(1)
                .size(5)
                .build();

        MemberSearch memberSearch = MemberSearch.builder()
                .searchDateType("")
                .searchType("name")
                .searchQuery("testName3")
                .build();

        //when
        PagingResponse<MemberProfile> pagingResponse = memberService.getList(memberSearch, pageRequest);

        //then
        assertEquals("test3@test.com", pagingResponse.getItems().get(0).getEmail());
        assertEquals("testName3", pagingResponse.getItems().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 초기화")
    void initPassword() {
        //given
        String encryptedPassword = passwordEncoder.encode("ValidPassword12!");
        Member member = Member.builder()
                .email("test@test.com")
                .password(encryptedPassword)
                .name("testName")
                .nickname("testNickname")
                .build();

        memberRepository.save(member);
        MemberResetPassword resetPassword = new MemberResetPassword("resetPassword");

        //when
        memberService.initPassword(member.getId(), resetPassword);

        //then
        assertTrue(passwordEncoder.matches(resetPassword.getPassword(), member.getPassword()));
    }
}