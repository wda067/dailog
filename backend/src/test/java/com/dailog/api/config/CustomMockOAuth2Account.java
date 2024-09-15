package com.dailog.api.config;

import com.dailog.api.domain.enums.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomMockOAuth2AccountSecurityContext.class)
public @interface CustomMockOAuth2Account {

    String username() default "oAuth2@test.com";

    String name() default "oAuth2Name";

    String nickname() default "oAuth2Nickname";

    Role role() default Role.MEMBER;

    String provider() default "naver";
}
