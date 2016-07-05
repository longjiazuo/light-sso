package org.light4j.sso.server.sso.web.sso;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.light4j.sso.common.utils.SSOConstants;
import org.light4j.sso.server.sso.system.service.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;


@Controller
@RequestMapping(value = "/sso")
public class SSOController extends SpringBeanAutowiringSupport {

    @Autowired
    private SSOService ssoService;

    @RequestMapping(value = "login", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {

        String message = null;
        String backUrl = null;

        if (request.getMethod().equals("GET")) {
            backUrl = request.getParameter(SSOConstants.BACK_URL);
        }
        else if (request.getMethod().equals("POST")) {
            backUrl = request.getParameter("backUrl");
        }

        do {
            if (backUrl == null) {
                message = "缺少回调URL";
                break;
            }
            boolean result = ssoService.validateBackUrl(backUrl);
            if (!result) {
                message = "无效的回调URL";
                break;
            }
            if (request.getMethod().equals("POST")) {
                String userName = request.getParameter("userName");
                if (userName != null) {
                    userName = userName.trim();
                }
                if (userName == null || userName.isEmpty()) {
                    message = "必须输入用户名";
                    break;
                }
                String password = request.getParameter("password");
                if (password != null) {
                    password = password.trim();
                }
                result = ssoService.validateLogin(userName, password, backUrl, request, response);
                if (!result) {
                    message = "用户名或者密码不正确";
                    break;
                }
            }
        } while (false);

        model.addAttribute("message", message == null ? "" : message);
        if (backUrl != null) {
            model.addAttribute("backUrl", backUrl);
        }

        return "sso/login";
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        String message = "登出成功";
        do {
            String backUrl = request.getParameter(SSOConstants.BACK_URL);
            if (backUrl == null) {
                message = "缺少回调URL";
                break;
            }
            boolean result = ssoService.validateBackUrl(backUrl);
            if (!result) {
                message = "无效的回调URL";
                break;
            }
            ssoService.handleLogout(backUrl, request, response);
        } while (false);

        model.addAttribute("message", message == null ? "" : message);
        return "sso/logout";
    }
}