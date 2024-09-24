package com.dailog.api.service;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.MemberEditor;
import com.dailog.api.domain.MemberEditor.MemberEditorBuilder;
import com.dailog.api.domain.enums.Role;
import com.dailog.api.exception.auth.EmailAlreadyExists;
import com.dailog.api.exception.auth.SamePassword;
import com.dailog.api.exception.auth.Unauthorized;
import com.dailog.api.exception.member.InvalidMemberPassword;
import com.dailog.api.exception.member.InvalidNickname;
import com.dailog.api.exception.member.InvalidPasswordConfirm;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.request.Join;
import com.dailog.api.request.Leave;
import com.dailog.api.request.member.MemberChangeNickname;
import com.dailog.api.request.member.MemberChangePassword;
import com.dailog.api.request.member.MemberPageRequest;
import com.dailog.api.request.member.MemberResetPassword;
import com.dailog.api.request.member.MemberSearch;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.member.MemberLoginInfo;
import com.dailog.api.response.member.MemberProfile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(Join join) {

        if (memberRepository.findByEmail(join.getEmail()).isPresent()) {
            throw new EmailAlreadyExists();
        }

        if (memberRepository.findByNickname(join.getNickname()).isPresent()) {
            throw new InvalidNickname();
        }

        String encryptedPassword = passwordEncoder.encode(join.getPassword());

        Member member = Member.builder()
                .name(join.getName())
                .password(encryptedPassword)
                .email(join.getEmail())
                .nickname(join.getNickname())
                .role(Role.MEMBER)
                .build();

        memberRepository.save(member);
    }

    public void joinAdmin(Join join) {

        if (memberRepository.findByEmail(join.getEmail()).isPresent()) {
            throw new EmailAlreadyExists();
        }

        String encryptedPassword = passwordEncoder.encode(join.getPassword());

        Member member = Member.builder()
                .name(join.getName())
                .nickname(join.getNickname())
                .password(encryptedPassword)
                .email(join.getEmail())
                .role(Role.ADMIN)
                .build();

        memberRepository.save(member);
    }
    //todo 회원 탈퇴 시 Refresh 토큰 삭제
    public void leave(Long memberId, Leave leave, String email) {
        Member member = getMember(memberId, email);

        boolean isPasswordMismatch = !passwordEncoder.matches(leave.getPassword(), member.getPassword());
        if (isPasswordMismatch) {
            throw new InvalidMemberPassword();
        }

        memberRepository.delete(member);
    }

    public void leaveOAuth2(Long memberId, String email) {
        memberRepository.delete(getMember(memberId, email));
    }

    public void delete(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFound::new);

        memberRepository.delete(member);
    }

    public MemberLoginInfo getLoginInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);

        return new MemberLoginInfo(member);
    }

    public MemberProfile getMemberProfile(Long memberId, String email) {
        Member member = getMember(memberId, email);

        return new MemberProfile(member);
    }

    public MemberProfile getProfileByAdmin(Long memberId) {
        return new MemberProfile(memberRepository.findById(memberId)
                .orElseThrow(MemberNotFound::new));
    }

    @Transactional
    public void changePassword(Long memberId, MemberChangePassword request, String email) {
        Member member = getMember(memberId, email);

        boolean isInvalidPassword = !passwordEncoder.matches(request.getCurrentPassword(), member.getPassword());
        if (isInvalidPassword) {
            throw new InvalidMemberPassword();
        }

        boolean isInvalidConfirm = !request.getNewPassword().equals(request.getConfirmPassword());
        if (isInvalidConfirm) {
            throw new InvalidPasswordConfirm();
        }

        boolean isSame = request.getCurrentPassword().equals(request.getNewPassword());
        if (isSame) {
            throw new SamePassword();
        }

        MemberEditorBuilder memberEditorBuilder = member.toEditor();

        if (request.getCurrentPassword() != null) {
            String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
            memberEditorBuilder.password(encryptedPassword);
        }

        MemberEditor memberEditor = memberEditorBuilder.build();

        member.edit(memberEditor);
    }

    @Transactional
    public void changeNickname(Long memberId, MemberChangeNickname request, String email) {
        Member member = getMember(memberId, email);

        if (member.getNickname().equals(request.getNewNickname())) {
            throw new InvalidNickname("기존 별명은 입력할 수 없습니다.");
        }

        if (memberRepository.findByNickname(request.getNewNickname()).isPresent()) {
            throw new InvalidNickname();
        }

        MemberEditorBuilder memberEditorBuilder = member.toEditor();
        MemberEditor memberEditor = memberEditorBuilder.nickName(request.getNewNickname()).build();
        member.edit(memberEditor);
    }

    //public List<MemberProfile> getMembers() {
    //    List<Member> members = memberRepository.findAll();
    //
    //    return members.stream()
    //            .map(member -> MemberProfile.builder()
    //                    .id(member.getId())
    //                    .email(member.getEmail())
    //                    .name(member.getName())
    //                    .createdAt(member.getCreatedAt())
    //                    .role(member.getRole())
    //                    .build())
    //            .collect(Collectors.toList());
    //}

    public PagingResponse<MemberProfile> getList(MemberPageRequest request) {
        Page<Member> memberPage = memberRepository.getList(request);

        return new PagingResponse<>(memberPage, MemberProfile.class);
    }

    public PagingResponse<MemberProfile> getList(MemberSearch memberSearch, MemberPageRequest request) {
        Page<Member> memberPage = memberRepository.getList(memberSearch, request);

        return new PagingResponse<>(memberPage, MemberProfile.class);
    }

    @Transactional
    public void initPassword(Long memberId, MemberResetPassword request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFound::new);

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        MemberEditor memberEditor = member.toEditor()
                .password(encryptedPassword)
                .build();

        member.edit(memberEditor);
    }

    private Member getMember(Long memberId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);

        boolean isNotSelf = !Objects.equals(member.getId(), memberId);
        if (isNotSelf) {
            throw new Unauthorized("접근 권한이 없습니다.");
        }

        return member;
    }
}
