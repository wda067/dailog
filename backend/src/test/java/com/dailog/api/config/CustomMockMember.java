package com.dailog.api.config;

import com.dailog.api.domain.enums.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomMockMemberSecurityContext.class)
public @interface CustomMockMember {

    String email() default "member@test.com";

    String name() default "Member";

    String nickname() default "Member";

    String password() default "ValidPassword12!";

    Role role() default Role.MEMBER;
}
