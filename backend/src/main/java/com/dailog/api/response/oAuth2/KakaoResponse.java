package com.dailog.api.response.oAuth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> properties;
    private final String id;

    public KakaoResponse(Map<String, Object> attribute) {
        this.properties = (Map<String, Object>) attribute.get("properties");
        this.id = attribute.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public String getEmail() {
        return getProviderId() + "@" + getProvider() + ".com";
    }

    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    public String getNickname() {
        return "KakaoUser_" + getProviderId().substring(0, 4);
    }
}
