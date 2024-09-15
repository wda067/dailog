package com.dailog.api.controller;

import com.dailog.api.config.CustomUserDetails;
import com.dailog.api.exception.InvalidRequest;
import com.dailog.api.exception.auth.Unauthorized;
import com.dailog.api.request.Join;
import com.dailog.api.request.Leave;
import com.dailog.api.request.member.MemberChangeNickname;
import com.dailog.api.request.member.MemberChangePassword;
import com.dailog.api.request.member.MemberPageRequest;
import com.dailog.api.request.member.MemberResetPassword;
import com.dailog.api.request.member.MemberSearch;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.member.MemberLoginInfo;
import com.dailog.api.response.member.MemberProfile;
import com.dailog.api.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/api/auth/join")
    public void join(@RequestBody @Validated Join join) {
        memberService.join(join);
    }

    @PostMapping("/api/auth/{memberId}/leave")
    public void leave(@PathVariable Long memberId,
                      @RequestBody @Validated Leave leave,
                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails != null) {
            memberService.leave(memberId, leave, customUserDetails.getUsername());
        } else {
            throw new Unauthorized();
        }
    }

    @PostMapping("/api/auth/{memberId}/leave-oauth2")
    public void leaveOAuth2(@PathVariable Long memberId,
                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        if (customOAuth2User != null) {
            memberService.leaveOAuth2(memberId, customOAuth2User.getUsername());
        } else {
            throw new Unauthorized();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/api/admin/{memberId}/delete")
    public void deleteByAdmin(@PathVariable Long memberId) {
        memberService.delete(memberId);
    }

    @GetMapping("/api/member/me")
    public MemberLoginInfo loginInfo(@AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            return memberService.getLoginInfo(username);
        }
        throw new Unauthorized();
    }

    @GetMapping("/api/members/{memberId}")
    public MemberProfile getProfile(@PathVariable("memberId") Long memberId,
                                    @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            return memberService.getMemberProfile(memberId, username);
        }
        throw new Unauthorized();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/admin/members/{memberId}")
    public MemberProfile getProfileByAdmin(@PathVariable("memberId") Long memberId) {
        return memberService.getProfileByAdmin(memberId);
    }

    @PatchMapping("/api/members/{memberId}/password")
    public void changePassword(@PathVariable Long memberId,
                               @RequestBody @Validated MemberChangePassword request,
                               @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            memberService.changePassword(memberId, request, username);
        }
    }

    @PatchMapping("/api/members/{memberId}/nickname")
    public void changeNickname(@PathVariable Long memberId,
                               @RequestBody @Validated MemberChangeNickname request,
                               @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            memberService.changeNickname(memberId, request, username);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/admin/members")
    public PagingResponse<MemberProfile> getMembers(MemberPageRequest memberPageRequest) {
        return memberService.getList(memberPageRequest);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/api/admin/members", params = {"searchDateType", "searchType", "searchQuery"})
    public PagingResponse<MemberProfile> getSearchList(MemberSearch memberSearch, MemberPageRequest memberPageRequest) {
        return memberService.getList(memberSearch, memberPageRequest);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/api/admin/members/{memberId}")
    public void initializePassword(@PathVariable("memberId") Long memberId,
                                   @RequestBody @Validated MemberResetPassword request) {
        memberService.initPassword(memberId, request);
    }

    private String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUsername();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUsername();
        }
        return null;
    }
}
