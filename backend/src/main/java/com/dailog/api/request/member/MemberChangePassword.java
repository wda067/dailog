package com.dailog.api.request.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberChangePassword {
    
    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private final String currentPassword;

    //@NotBlank(message = "새로운 비밀번호를 입력해 주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "유효하지 않은 비밀번호입니다."
    )
    private final String newPassword;

    @NotBlank(message = "새로운 비밀번호를 한 번 더 입력해 주세요.")
    private final String confirmPassword;

    @Builder
    public MemberChangePassword(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
