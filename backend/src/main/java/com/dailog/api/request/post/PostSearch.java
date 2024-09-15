package com.dailog.api.request.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearch {

    private String searchDateType;  //등록일을 기준으로 조회
    private String searchType;  //제목 또는 내용으로 검색
    private String searchQuery = "";
}
