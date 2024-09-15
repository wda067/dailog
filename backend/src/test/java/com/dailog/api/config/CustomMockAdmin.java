package com.dailog.api.config;

import com.dailog.api.domain.enums.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomMockAdminSecurityContext.class)
public @interface CustomMockAdmin {

    String email() default "admin@test.com";

    String name() default "Admin";

    String nickname() default "Admin";

    String password() default "ValidPassword12!";

    Role role() default Role.ADMIN;
}
