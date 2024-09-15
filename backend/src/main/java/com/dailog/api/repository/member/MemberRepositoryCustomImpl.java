package com.dailog.api.repository.member;

import static com.dailog.api.domain.QMember.member;

import com.dailog.api.domain.Member;
import com.dailog.api.request.member.MemberPageRequest;
import com.dailog.api.request.member.MemberSearch;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> getList(MemberPageRequest memberPageRequest) {

        Long totalCount = queryFactory.select(member.count())
                .from(member)
                .fetchFirst();

        List<Member> members = queryFactory.selectFrom(member)
                .limit(memberPageRequest.getSize())
                .offset(memberPageRequest.getOffset())
                .orderBy(member.id.desc())
                .fetch();

        return new PageImpl<>(members, memberPageRequest.getPageable(), totalCount);
    }

    @Override
    public Page<Member> getList(MemberSearch memberSearch, MemberPageRequest memberPageRequest) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(regDtsAfter(memberSearch.getSearchDateType()));
        builder.and(searchByLike(memberSearch.getSearchQuery(), memberSearch.getSearchType()));

        Long count = queryFactory.select(member.count())
                .from(member)
                .where(builder)
                .fetchFirst();

        List<Member> members = queryFactory.selectFrom(member)
                .where(builder)
                .limit(memberPageRequest.getSize())
                .offset(memberPageRequest.getOffset())
                .orderBy(member.id.desc())
                .fetch();

        return new PageImpl<>(members, memberPageRequest.getPageable(), count);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (searchDateType == null) {
            return null;
        }

        switch (searchDateType) {
            case "1w" -> dateTime = dateTime.minusWeeks(1);
            case "1m" -> dateTime = dateTime.minusMonths(1);
            case "6m" -> dateTime = dateTime.minusMonths(6);
            case "1y" -> dateTime = dateTime.minusYears(1);
            default -> {
                return null;
            }
        }

        return member.createdAt.after(dateTime)
                .or(member.createdAt.eq(dateTime));
    }

    private BooleanExpression searchByLike(String searchQuery, String searchType) {
        if (searchQuery == null || searchType == null) {
            return null;
        }

        BooleanExpression nameExpression = member.name.like("%" + searchQuery + "%");
        BooleanExpression emailExpression = member.email.like("%" + searchQuery + "%");
        BooleanExpression roleExpression = member.role.stringValue().like("%" + searchQuery + "%");

        switch (searchType) {
            case "name" -> {
                return nameExpression;
            }
            case "email" -> {
                return emailExpression;
            }
            case "role" -> {
                return roleExpression;
            }
            default -> {
                return null;
            }
        }
    }
}
