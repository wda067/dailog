package com.dailog.api.request.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MemberSearch {

    private final String searchDateType;
    private final String searchType;
    private String searchQuery = "";

    @Builder
    public MemberSearch(String searchDateType, String searchType, String searchQuery) {
        this.searchDateType = searchDateType;
        this.searchType = searchType;
        this.searchQuery = searchQuery;
    }
}
