package com.dailog.api.domain.enums;

import com.dailog.api.exception.member.RoleNotFound;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum Role {
    MEMBER(List.of("ROLE_MEMBER")),
    ADMIN(List.of("ROLE_ADMIN", "ROLE_MEMBER"));

    private final List<String> authorities;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public static Role findByAuthority(Collection<? extends GrantedAuthority> authorities) {
        Set<String> authoritySet = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Arrays.stream(Role.values())
                .filter(role -> new HashSet<>(role.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                        .containsAll(authoritySet))
                .findFirst()
                .orElseThrow(RoleNotFound::new);
    }
}
