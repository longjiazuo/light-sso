package org.light4j.sso.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.light4j.sso.client.utils.FilterConstants;
import org.light4j.sso.common.msg.SSORequest;
import org.light4j.sso.common.user.User;
import org.light4j.sso.common.utils.DESedeUtils;
import org.light4j.sso.common.utils.HttpClient;
import org.light4j.sso.common.utils.SSOConstants;
import org.light4j.sso.common.utils.SSOException;
import org.light4j.sso.common.utils.SSOUtil;
import org.light4j.sso.common.utils.Signer;
import org.light4j.sso.common.utils.HttpClient.HttpResult;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by longjiazuo on 2015/5/18.
 */
public class SSOClient {

    private static String serverHost;
    private static String backUrl;
    private static String appKey;
    private static String appSecret;

    public static void setServerHost(String server) {
        serverHost = server;
    }

    public static void setBackUrl(String url) {
        backUrl = url;
    }

    public static void setAppKey(String key) {
        appKey = key;
    }

    public static void setAppSecret(String secret) {
        appSecret = secret;
    }

    public static User getLoginUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute(FilterConstants.SESSION_ATTR_USER);
        if (user != null) {
            return user;
        }
        String token = SSOUtil.getCookieValue(request, SSOConstants.SSO_TOKEN);
        if (token == null || token.isEmpty()) {
            return null;
        }

        String ssoInfo = buildSSOInfo(token);
        Map<String, Object> reqBody = new HashMap<String, Object>();
        reqBody.put(SSOConstants.ApiParam.SSO_INFO, ssoInfo);
        String apiUrl = FilterConstants.USER_SCHEME + "://" + serverHost + FilterConstants.GET_USER_URI;
        HttpResult result = callAPI(apiUrl, reqBody);
        if (result.code != HttpURLConnection.HTTP_OK) {
            throw new SSOException("Get user request failed, http-code:" + result.code + " http-content:" + result.content);
        }
        JSONObject obj = null;
        try {
            obj = JSONObject.parseObject(result.content);
        } catch (Exception e) {
        }
        if (obj == null) {
            throw new SSOException("Invalid response: " + result.content);
        }
        Integer status = obj.getInteger(SSOConstants.ApiParam.STATUS);
        Integer code = obj.getInteger(SSOConstants.ApiParam.CODE);
        String message = obj.getString(SSOConstants.ApiParam.MESSAGE);
        JSONObject content = obj.getJSONObject(SSOConstants.ApiParam.CONTENT);
        if (status == null || code == null || status != 0 || code != 0 || content == null) {
            throw new SSOException("Get user info failed, code:" + code + " message:" + message);
        }
        String encUser = content.getString(SSOConstants.ApiParam.LOGIN_USER);
        user = decryptUser(encUser);
        request.getSession().setAttribute(FilterConstants.SESSION_ATTR_USER, user);
        return user;
    }

    public static void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SSOUtil.sendRedirect(response, request, buildLoginUrl());
    }

    public static void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SSOUtil.sendRedirect(response, request, buildLogoutUrl());
    }

    private static String buildLoginUrl() {
        try {
            return FilterConstants.LOGIN_SCHEME + "://" + serverHost + FilterConstants.LOGIN_PAGE_URI +
                    "?" + SSOConstants.BACK_URL + "=" + URLEncoder.encode(backUrl, SSOConstants.URL_ENCODING);
        } catch (Exception e) {
            throw new SSOException(e);
        }
    }

    private static String buildLogoutUrl() {
        try {
            return FilterConstants.LOGOUT_SCHEME + "://" + serverHost + FilterConstants.LOGOUT_PAGE_URI +
                    "?" + SSOConstants.BACK_URL + "=" + URLEncoder.encode(backUrl, SSOConstants.URL_ENCODING);
        } catch (Exception e) {
            throw new SSOException(e);
        }
    }

    private static String buildSSOInfo(String token) {
        DESedeUtils encoder = new DESedeUtils();
        if (!encoder.init(DESedeUtils.ENCRYPT_MODE, appSecret)) {
            throw new RuntimeException("Invalid secretKey");
        }
        SSORequest ssoReq = new SSORequest();
        ssoReq.setToken(token);
        return encoder.encrypt(JSONObject.toJSONString(ssoReq));
    }

    private static HttpResult callAPI(String url, Map<String, Object> body) {
        body.put(SSOConstants.ApiParam.VERSION, FilterConstants.API_VERSION);
        body.put(SSOConstants.ApiParam.TIMESTAMP, System.currentTimeMillis());
        String content = JSONObject.toJSONString(body);
        Map<String, String> properties = new HashMap<String, String>();
        String signature;
        try {
            signature = Signer.sign(content, appSecret);
        } catch (Exception e) {
            throw new RuntimeException("Signature failed", e);
        }
        properties.put(SSOConstants.ApiParam.ACCESS_KEY, appKey);
        properties.put(SSOConstants.ApiParam.SIGNATURE, signature);
        HttpResult resp;
        try {
            resp = HttpClient.httpPostJson(url, properties, content, 30000);
        } catch (IOException e) {
            throw new RuntimeException("Send request failed: " + url, e);
        }
        return resp;
    }

    private static User decryptUser(String encUser) {
        DESedeUtils decoder = new DESedeUtils();
        if (!decoder.init(DESedeUtils.DECRYPT_MODE, appSecret)) {
            throw new RuntimeException("Invalid secretKey");
        }
        String userInfo = decoder.decrypt(encUser);
        if (userInfo == null) {
            throw new RuntimeException("Decrypt user failed");
        }
        User user = null;
        try {
            user = JSONObject.parseObject(userInfo, User.class);
        } catch (Exception e) {
        }
        if (user == null) {
            throw new RuntimeException("Parse user failed:" + userInfo);
        }
        return user;
    }

    public static boolean isUserExist(String name) throws IOException {

        Map<String, Object> reqBody = new HashMap<String, Object>();
        reqBody.put(SSOConstants.ApiParam.USER_NAME, name);
        String apiUrl = FilterConstants.USER_SCHEME + "://" + serverHost + FilterConstants.CHECK_USER_URI;
        HttpResult result = callAPI(apiUrl, reqBody);
        if (result.code != HttpURLConnection.HTTP_OK) {
            throw new SSOException("Check user request failed, http-code:" + result.code + " http-content:" + result.content);
        }
        JSONObject obj = null;
        try {
            obj = JSONObject.parseObject(result.content);
        } catch (Exception e) {
        }
        if (obj == null) {
            throw new SSOException("Invalid response: " + result.content);
        }
        Integer status = obj.getInteger(SSOConstants.ApiParam.STATUS);
        Integer code = obj.getInteger(SSOConstants.ApiParam.CODE);
        return status == 0 && code == 0;
    }
}
