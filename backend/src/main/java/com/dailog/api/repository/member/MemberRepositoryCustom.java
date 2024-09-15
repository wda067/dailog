package com.dailog.api.repository.member;


import com.dailog.api.domain.Member;
import com.dailog.api.request.member.MemberPageRequest;
import com.dailog.api.request.member.MemberSearch;
import org.springframework.data.domain.Page;

public interface MemberRepositoryCustom {

    Page<Member> getList(MemberPageRequest memberPageRequest);

    Page<Member> getList(MemberSearch memberSearch, MemberPageRequest memberPageRequest);
}
