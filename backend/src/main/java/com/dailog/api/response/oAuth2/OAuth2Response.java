package com.dailog.api.response.oAuth2;

public interface OAuth2Response {

    //공급자 (Ex. naver, google, ...)
    String getProvider();
    //공급자에서 발급해주는 아이디(번호)
    String getProviderId();
    //이메일
    String getEmail();
    //사용자 실명 (설정한 이름)
    String getName();
    //별명
    String getNickname();
}
