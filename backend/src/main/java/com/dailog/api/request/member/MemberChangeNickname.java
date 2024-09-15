package com.dailog.api.request.member;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class MemberChangeNickname {

    @NotBlank(message = "변경할 별명을 입력해 주세요.")
    @Size(max = 8, message = "별명은 8자까지 입력해 주세요.")
    private final String newNickname;

    @JsonCreator
    public MemberChangeNickname(String newNickname) {
        this.newNickname = newNickname;
    }
}
