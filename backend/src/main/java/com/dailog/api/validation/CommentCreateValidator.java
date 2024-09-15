package com.dailog.api.validation;

import com.dailog.api.request.comment.CommentCreateForAnonymous;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class CommentCreateValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CommentCreateForAnonymous.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CommentCreateForAnonymous commentCreateForAnonymous = (CommentCreateForAnonymous) target;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("CommentCreateValidator");
        log.info("Authentication : {}", authentication);
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        if (isLoggedIn) {
            if (commentCreateForAnonymous.getAnonymousName() != null && !commentCreateForAnonymous.getAnonymousName().isEmpty()) {
                errors.rejectValue("anonymousName", "Invalid", "로그인된 경우 이름을 입력할 수 없습니다.");
            }
            if (commentCreateForAnonymous.getPassword() != null && !commentCreateForAnonymous.getPassword().isEmpty()) {
                errors.rejectValue("password", "Invalid", "로그인된 경우 비밀번호를 입력할 수 없습니다.");
            }
        } else {
            if (commentCreateForAnonymous.getAnonymousName() == null || commentCreateForAnonymous.getAnonymousName().isEmpty()) {
                errors.rejectValue("anonymousName", "NotBlank", "이름을 입력해 주세요.");
            } else if (commentCreateForAnonymous.getAnonymousName().length() > 8) {
                errors.rejectValue("anonymousName", "Length", "이름은 1~8자로 입력해 주세요.");
            }

            if (commentCreateForAnonymous.getPassword() == null || commentCreateForAnonymous.getPassword().isEmpty()) {
                errors.rejectValue("password", "NotBlank", "비밀번호를 입력해주세요.");
            } else if (commentCreateForAnonymous.getPassword().length() < 4 || commentCreateForAnonymous.getPassword().length() > 30) {
                errors.rejectValue("password", "Length", "비밀번호는 4~30자로 입력해 주세요.");
            }
        }

        if (commentCreateForAnonymous.getContent() == null || commentCreateForAnonymous.getContent().isEmpty()) {
            errors.rejectValue("content", "NotBlank", "내용을 입력해주세요.");
        } else if (commentCreateForAnonymous.getContent().length() > 1000) {
            errors.rejectValue("content", "Length", "내용은 1000자까지 작성해 주세요.");
        }
    }
}
