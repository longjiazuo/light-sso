package org.light4j.sso.server.uic.web.system.account;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.light4j.sso.client.SSOClient;
import org.light4j.sso.common.user.User;
import org.light4j.sso.server.uic.common.exception.ServiceException;
import org.light4j.sso.server.uic.system.account.entity.Account;
import org.light4j.sso.server.uic.system.account.entity.SecurityCredential;
import org.light4j.sso.server.uic.system.account.entity.UserInfo;
import org.light4j.sso.server.uic.system.account.entity.UserInfoDelta;
import org.light4j.sso.server.uic.system.account.service.AccountService;
import org.light4j.sso.server.uic.utils.ThreadContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;


@Controller
@RequestMapping(value = "/account")
public class AccountController extends SpringBeanAutowiringSupport {

    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "init", method = {RequestMethod.GET, RequestMethod.POST})
    public String init(HttpServletRequest request, Model model) throws IOException {

        String message = null;
        boolean done = false;
        if (request.getMethod().equals("GET")) {
            if (accountService.initDone()) {
                done = true;
                message = "初始化成功";
            }
        }
        else {
            do {
                String appKey = request.getParameter("appKey");
                if (appKey != null) {
                    appKey = appKey.trim();
                }
                if (appKey == null || appKey.isEmpty()) {
                    message = "必须输入系统APP_KEY";
                    break;
                }
                String appSecret = request.getParameter("appSecret");
                if (appSecret != null) {
                    appSecret = appSecret.trim();
                }
                if (appSecret == null || appSecret.isEmpty()) {
                    message = "必须输入系统APP_SECRET";
                    break;
                }
                String userName = request.getParameter("userName");
                if (userName != null) {
                    userName = userName.trim();
                }
                if (userName == null || userName.isEmpty()) {
                    message = "必须输入管理员账号";
                    break;
                }
                String password = request.getParameter("password");
                if (password != null) {
                    password = password.trim();
                }
                if (password == null || password.isEmpty()) {
                    message = "必须输入管理员密码";
                    break;
                }
                accountService.init(appKey, appSecret, userName, password);
                message = "初始化成功";
                done = true;
                break;
            } while (true);
        }

        model.addAttribute("message", message == null ? "" : message);

        return done ? "account/notice" : "account/init";
    }

    @RequestMapping(value = "register", method = {RequestMethod.GET, RequestMethod.POST})
    public String register(HttpServletRequest request, Model model) throws IOException {

        if (request.getMethod().equals("GET")) {
            model.addAttribute("message", "");
            return "account/register";
        } else {
            String userName = request.getParameter("userName");
            if (userName != null) {
                userName = userName.trim();
            }
            if (userName == null || userName.isEmpty()) {
                model.addAttribute("message", "必须输入用户名");
                return "account/register";
            }
            String password = request.getParameter("password");
            if (password != null) {
                password = password.trim();
            }
            if (password == null || password.isEmpty()) {
                model.addAttribute("message", "必须输入密码");
                return "account/register";
            }
            UserInfoDelta info = new UserInfoDelta();
            info.setPassword(password);
            String birthplace = request.getParameter("birthplace");
            if (birthplace != null) {
                birthplace = birthplace.trim();
            }
            info.setBirthplace(birthplace);
            String location = request.getParameter("location");
            if (location != null) {
                location = location.trim();
            }
            info.setLocation(location);
            String misc = request.getParameter("misc");
            if (misc != null) {
                misc = misc.trim();
            }
            info.setMisc(misc);
            try {
                accountService.registerUser(userName, info);
            } catch (ServiceException e) {
                String message;
                switch (e.getError()) {
                    case DB_DUPLICATE_KEY:
                        message = "用户名已存在";
                        break;
                    case CONTAIN_RESERVED_CHAR:
                        message = "用户名包含保留字符";
                        break;
                    default:
                        message = e.getError().getErrorMessage();
                }
                model.addAttribute("message", message);
                return "account/register";
            }
            return "redirect:/";
        }
    }

    @RequestMapping(value = "update", method = {RequestMethod.GET, RequestMethod.POST})
    public String update(HttpServletRequest request, Model model) throws IOException {

        User loginUser = ThreadContextUtil.getLoginUser();
        if (loginUser == null) {
            model.addAttribute("message", "用户未登录");
            return "account/update";
        }
        Account user = accountService.getAccountById(loginUser.getUid());
        if (user == null) {
            model.addAttribute("message", "用户不存在");
            return "account/update";
        }
        model.addAttribute("userName", user.getName());
        if (request.getMethod().equals("GET")) {
            UserInfo userInfo = accountService.getUserInfo(user.getId());
            if (userInfo != null) {
                model.addAttribute("birthplace", userInfo.getBirthplace());
                model.addAttribute("location", userInfo.getLocation());
                model.addAttribute("misc", userInfo.getMisc());
            }
            model.addAttribute("message", "");
            return "account/update";
        }
        else {
            UserInfoDelta delta = new UserInfoDelta();
            delta.setUserId(user.getId());
            String password = request.getParameter("password");
            if (password != null) {
                password = password.trim();
            }
            if (password != null && !password.isEmpty()) {
                delta.setPassword(password);
            }
            String birthplace = request.getParameter("birthplace");
            if (birthplace != null && !birthplace.isEmpty()) {
                birthplace = birthplace.trim();
                delta.setBirthplace(birthplace);
            }
            String location = request.getParameter("location");
            if (location != null && !location.isEmpty()) {
                location = location.trim();
                delta.setLocation(location);
            }
            String misc = request.getParameter("misc");
            if (misc != null && !misc.isEmpty()) {
                misc = misc.trim();
                delta.setMisc(misc);
            }
            accountService.updateUserInfo(delta);
            model.addAttribute("birthplace", birthplace);
            model.addAttribute("location", location);
            model.addAttribute("misc", misc);
            model.addAttribute("message", "更新成功");
            return "account/update";
        }
    }

    @RequestMapping(value = "app", method = {RequestMethod.GET, RequestMethod.POST})
    public String app(HttpServletRequest request, Model model) throws IOException {

        if (request.getMethod().equals("GET")) {
            model.addAttribute("message", "");
            return "account/app";
        }
        else {
            String message = "添加成功！请妥善保管应用的KEY";
            check_loop:
            do {
                String appName = request.getParameter("appName");
                if (appName != null) {
                    appName = appName.trim();
                }
                if (appName == null || appName.isEmpty()) {
                    message = "必须输入应用名";
                    break;
                }
                model.addAttribute("appName", appName);
                SecurityCredential credential = null;
                try {
                    credential = accountService.addApp(appName);
                } catch (ServiceException e) {
                    switch (e.getError()) {
                        case DB_DUPLICATE_KEY:
                            message = "应用名已存在";
                            break check_loop;
                        case CONTAIN_RESERVED_CHAR:
                            message = "应用名包含保留字符";
                            break check_loop;
                        default:
                            message = e.getError().getErrorMessage();
                            break check_loop;
                    }
                }

                if (credential != null) {
                    model.addAttribute("appKey",    "APP_KEY   : " + credential.getAccessKey());
                    model.addAttribute("appSecret", "APP_SECRET: " + credential.getSecretKey());
                }
                break;
            } while (true);
            model.addAttribute("message", message);
            return "account/app";
        }
    }

    @RequestMapping(value = "batch", method = {RequestMethod.GET, RequestMethod.POST})
    public String batch(HttpServletRequest request, Model model) throws IOException {

        if (request.getMethod().equals("GET")) {
            model.addAttribute("start", 1);
            model.addAttribute("message", "");
            return "account/batch";
        }
        else {
            String message = "";
            do {
                String countStr = request.getParameter("count");
                if (countStr == null || countStr.trim().isEmpty()) {
                    message = "必须输入用户个数";
                    break;
                }
                int count = Integer.valueOf(countStr);
                if (count <= 0) {
                    message = "非法的用户个数";
                    break;
                }
                int start = 1;
                String startStr = request.getParameter("start");
                if (startStr != null && !startStr.trim().isEmpty()) {
                    start = Integer.valueOf(startStr);
                }
                if (start < 0) {
                    message = "非法的开始序号";
                    break;
                }
                String namePrefix = request.getParameter("namePrefix");
                if (namePrefix != null) {
                    namePrefix = namePrefix.trim();
                }
                if (namePrefix == null || namePrefix.isEmpty()) {
                    message = "必须输入用户名前缀";
                    break;
                }
                String password = request.getParameter("password");
                if (password != null) {
                    password = password.trim();
                }
                if (password == null || password.isEmpty()) {
                    message = "必须输入密码";
                    break;
                }
                String idStartStr = request.getParameter("idStart");
                Long idStart = (idStartStr != null && !idStartStr.trim().isEmpty()) ? Long.valueOf(idStartStr) : null;
                UserInfoDelta info = new UserInfoDelta();
                info.setPassword(password);
                String birthplace = request.getParameter("birthplace");
                if (birthplace != null) {
                    birthplace = birthplace.trim();
                }
                info.setBirthplace(birthplace);
                String location = request.getParameter("location");
                if (location != null) {
                    location = location.trim();
                }
                info.setLocation(location);
                String misc = request.getParameter("misc");
                if (misc != null) {
                    misc = misc.trim();
                }
                info.setMisc(misc);
                String levelStr = request.getParameter("level");
                if (levelStr != null) {
                    int level = Integer.valueOf(levelStr.trim());
                    info.setLevel(level);
                }
                String loanPointStr = request.getParameter("loanPoint");
                if (loanPointStr != null) {
                    float loanPoint = Float.valueOf(loanPointStr);
                    info.setLoanPoint(loanPoint);
                }
                String lendPointStr = request.getParameter("lendPoint");
                if (lendPointStr != null) {
                    float lendPoint = Float.valueOf(lendPointStr);
                    info.setLendPoint(lendPoint);
                }
                int delta = 0;
                try {
                    delta = accountService.batchRegister(namePrefix, idStart, info, count, start);
                } catch (ServiceException e) {
                    switch (e.getError()) {
                        case CONTAIN_RESERVED_CHAR:
                            message = "用户名包含保留字符";
                            break;
                        default:
                            message = e.getError().getErrorMessage();
                    }
                    break;
                }
                message = "新建用户数" + delta;
                break;
            } while (true);

            model.addAttribute("message", message);
            return "account/batch";
        }
    }

    @RequestMapping(value = "check", method = {RequestMethod.GET, RequestMethod.POST})
    public String check(HttpServletRequest request, Model model) throws IOException {

        if (request.getMethod().equals("GET")) {
            model.addAttribute("message", "");
            return "account/check";
        } else {
            String userName = request.getParameter("userName");
            if (userName != null) {
                userName = userName.trim();
            }
            if (userName == null || userName.isEmpty()) {
                model.addAttribute("message", "必须输入用户名");
                return "account/check";
            }
            boolean exist = SSOClient.isUserExist(userName);
            model.addAttribute("message", exist ? "存在" : "不存在");
            return "account/check";
        }
    }
}