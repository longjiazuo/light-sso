package org.light4j.sso.common.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by longjiazuo on 2015/5/16.
 */
public class SSOUtil {

    private static final String HEADER_SCHEME = "X-Client-Scheme";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        if (cookieName == null || cookieName.isEmpty()) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    public static String decodeCookie(Cookie cookie) {
        if (cookie == null || cookie.getValue() == null) {
            return null;
        }
        try {
            return URLDecoder.decode(cookie.getValue(), SSOConstants.URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SSOException(e);
        }
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return decodeCookie(getCookie(request, cookieName));
    }

    public static void delCookie(HttpServletResponse response, String domain, String cookieName) {
        if (cookieName == null || cookieName.isEmpty()) {
            return;
        }
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setDomain(domain);
        response.addCookie(cookie);
    }

    public static void addCookie(HttpServletResponse response, String domain, String cookieName, String value, Integer maxAge) {
        try {
            Cookie cookie = new Cookie(cookieName, URLEncoder.encode(value, SSOConstants.URL_ENCODING));
            cookie.setPath("/");
            cookie.setDomain(domain);
            if (maxAge != null) {
                cookie.setMaxAge(maxAge);
            }
            response.addCookie(cookie);
        } catch (Exception e) {
            throw new SSOException(e);
        }
    }

    public static void sendRedirect(HttpServletResponse response, HttpServletRequest request, String url) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
            response.sendRedirect(url);
            return;
        }
        StringBuilder fullUrl = new StringBuilder();
        String requestScheme = request.getScheme();
        String headerScheme = request.getHeader(HEADER_SCHEME);
        if (HTTPS.equalsIgnoreCase(requestScheme) || HTTPS.equalsIgnoreCase(headerScheme)) {
            fullUrl.append(HTTPS);
        }
        else {
            fullUrl.append(HTTP);
        }
        fullUrl.append(url.startsWith("//") ? ":" : "://").append(url);
        response.sendRedirect(fullUrl.toString());
    }
}
