package org.light4j.sso.server.sso.system.service;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.light4j.sso.common.msg.SSORequest;
import org.light4j.sso.common.user.User;
import org.light4j.sso.common.utils.DESedeUtils;
import org.light4j.sso.common.utils.SSOConstants;
import org.light4j.sso.common.utils.SSOUtil;
import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;
import org.light4j.sso.server.uic.common.exception.ServiceException;
import org.light4j.sso.server.uic.system.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * Created by longjiazuo on 2015/5/14.
 */
@Component
@Slf4j
public class SSOService {
    private static final String DOMAIN_OPTION = "sso.domain";
    private static final String DEFAULT_DOMAIN = "light4j.com";
    private static final int COOKIE_EXPIRE = 1209600;// 2 weeks

    private static String myDomain;

    @Autowired
    private AccountService accountService;
    @Autowired
    private SSOHandler ssoHandler;

    public SSOService() {
        myDomain = System.getProperty(DOMAIN_OPTION);
        if (myDomain == null) {
            myDomain = DEFAULT_DOMAIN;
        }
    }

    public void handleLogout(String backUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ssoHandler.handleLogout(backUrl, request, response);
    }

    public boolean validateLogin(String userName, String password, String backUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ssoHandler.validateLogin(userName, password, backUrl, request, response);
    }

    public User getLoginUser(SSORequest request) {
        return ssoHandler.getLoginUser(request);
    }

    public boolean validateBackUrl(String url) {
        return domainMatch(myDomain, retrieveDomain(url));
    }

    private boolean domainMatch(String target, String source) {
        if (target == null || target.isEmpty() || source == null || source.isEmpty()) {
            return false;
        }
        if (target.charAt(0) == '.') {
            target = target.substring(1);
        }
        if (source.charAt(0) == '.') {
            source = source.substring(1);
        }
        if (target.equals(source)) {
            return true;
        }
        if (source.endsWith(target) && source.charAt(source.length() - target.length() - 1) == '.') {
            return true;
        }
        return false;
    }

    private String retrieveDomain(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int start = url.indexOf("//");
        if (start >= 0) {
            start += 2;
        }
        else {
            start = 0;
        }
        int end = url.indexOf("/", start);
        if (end < 0) {
            end = url.length();
        }
        int port = url.indexOf(":", start);
        if (port >= 0 && port < end) {
            end = port;
        }
        return url.substring(start, end);
    }

    public void loginReturn(String content, String backUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        setSSOToken(content, response);
        SSOUtil.sendRedirect(response, request, backUrl);
    }

    public void setSSOToken(String content, HttpServletResponse response) {
        String secretKey = accountService.getPrimarySecretKey();
        if (secretKey == null) {
            log.error("Failed to get primary secret key");
            throw new ServiceException(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
        }
        SSOToken token = new SSOToken();
        token.setContent(content);
        token.setTimestamp(System.currentTimeMillis());
        String tokenStr = JSON.toJSONString(token);
        DESedeUtils encoder = new DESedeUtils();
        encoder.init(DESedeUtils.ENCRYPT_MODE, secretKey);
        String encToken = encoder.encrypt(tokenStr);
        addCookie(response, SSOConstants.SSO_TOKEN, encToken);
    }

    public void removeSSOToken(HttpServletResponse response) {
        delCookie(response, SSOConstants.SSO_TOKEN);
    }

    public void addCookie(HttpServletResponse response, String name, String value) {
        SSOUtil.addCookie(response, myDomain, name, value, COOKIE_EXPIRE);
    }

    public void delCookie(HttpServletResponse response, String name) {
        SSOUtil.delCookie(response, myDomain, name);
    }

    public String decodeSSOToken(String encToken) {
        String secretKey = accountService.getPrimarySecretKey();
        if (secretKey == null) {
            log.error("Failed to get primary secret key");
            throw new ServiceException(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
        }
        DESedeUtils decoder = new DESedeUtils();
        decoder.init(DESedeUtils.DECRYPT_MODE, secretKey);
        String tokenStr = decoder.decrypt(encToken);
        if (tokenStr == null) {
            log.error("Failed to decrypt sso token:" + encToken);
            throw new ServiceException(ErrorCodeEnum.INVALID_SSO_TOKEN);
        }
        SSOToken token = null;
        try {
            token = JSON.parseObject(tokenStr, SSOToken.class);
        } finally {
            if (token == null) {
                log.error("Failed to parse sso token");
                throw new ServiceException(ErrorCodeEnum.INVALID_SSO_TOKEN);
            }
        }
        long age = System.currentTimeMillis() - token.getTimestamp();
        if (age < 0 || age > COOKIE_EXPIRE * 1000) {
            throw new ServiceException(ErrorCodeEnum.EXPIRED_SSO_TOKEN);
        }
        return token.getContent();
    }

}
