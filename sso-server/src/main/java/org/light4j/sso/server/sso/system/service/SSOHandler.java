package org.light4j.sso.server.sso.system.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.light4j.sso.common.msg.SSORequest;
import org.light4j.sso.common.user.User;
import org.light4j.sso.common.utils.SSOUtil;
import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;
import org.light4j.sso.server.uic.common.exception.ServiceException;
import org.light4j.sso.server.uic.system.account.entity.Account;
import org.light4j.sso.server.uic.system.account.entity.UserInfo;
import org.light4j.sso.server.uic.system.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by longjiazuo on 2015/5/14.
 */
@Component
@Slf4j
public class SSOHandler {


    @Autowired
    private AccountService accountService;
    @Autowired
    private SSOService ssoService;

    public void handleLogout(String backUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ssoService.removeSSOToken(response);
        SSOUtil.sendRedirect(response, request, backUrl);
    }

    public boolean validateLogin(String userName, String password, String backUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account user = accountService.getUser(userName);
        if (user != null) {
            UserInfo userInfo = accountService.getUserInfo(user.getId());
            String realPassword = (userInfo == null ? null : userInfo.getPassword());
            if (password == null && realPassword != null ||
                password != null && !password.equals(realPassword)) {
                return false;
            }
            ssoService.loginReturn(String.valueOf(user.getId()), backUrl, request, response);
            return true;
        } else {
            return false;
        }
    }

    public User getLoginUser(SSORequest request) {
        User user = new User();
        String token = request.getToken();
        String content = null;
        if (token != null && !token.isEmpty()) {
            content = ssoService.decodeSSOToken(token);
        }
        if (content != null) {
            long userId = Long.parseLong(content);
            Account account = accountService.getAccountById(userId);
            if (account == null) {
                throw new ServiceException(ErrorCodeEnum.USER_NOT_EXIST);
            }
            user.setUid(userId);
            user.setName(account.getName());
            UserInfo userInfo = accountService.getUserInfo(userId);
            if (userInfo != null) {
                user.setMisc(userInfo.getMisc());
            }
        }
        return user;
    }

}
