package com.dailog.api.request.member;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberResetPassword {

    @Size(min = 4, max = 16, message = "비밀번호는 4자~16자로 입력해 주세요.")
    private final String password;

    @JsonCreator
    public MemberResetPassword(String password) {
        this.password = password;
    }
}
