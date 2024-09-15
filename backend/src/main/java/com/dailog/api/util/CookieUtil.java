package com.dailog.api.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createCookie(String key, String value, int expiredS) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(expiredS);
        //cookie.setSecure(true);  //https에서만 쿠키 가능
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
