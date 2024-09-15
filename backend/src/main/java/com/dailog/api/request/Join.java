package com.dailog.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

/**
 * (?=.*[a-z]) : 최소 하나의 소문자 포함 (?=.*[A-Z]) : 최소 하나의 대문자 포함 (?=.*\\d) : 최소 하나의 숫자 포함 (?=.*[@$!%*?&]) : 최소 하나의 특수문자 포함
 * [A-Za-z\\d@$!%*?&]{8,} : 허용된 문자 집합으로 8자 이상
 */
@Getter
@Builder
public class Join {

    @Email(message = "이메일 형식으로 입력해 주세요.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "유효하지 않은 비밀번호입니다."
    )
    private String password;

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 8, message = "이름은 8자까지 입력해 주세요.")
    private String name;

    @NotBlank(message = "별명을 입력해 주세요.")
    @Size(max = 8, message = "별명은 8자까지 입력해 주세요.")
    private String nickname;
}
