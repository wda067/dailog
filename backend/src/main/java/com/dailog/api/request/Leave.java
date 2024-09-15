package com.dailog.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Leave {

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private final String password;

    @JsonCreator
    public Leave(String password) {
        this.password = password;
    }
}
