package org.light4j.sso.server.sso.api;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.light4j.sso.common.utils.SSOConstants;
import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;
import org.light4j.sso.server.uic.system.account.entity.Account;
import org.light4j.sso.server.uic.system.account.service.AccountService;
import org.light4j.sso.server.uic.utils.ThreadContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

@Slf4j
public abstract class RestApiBaseController extends SpringBeanAutowiringSupport {

    public static final long REQ_TIMEOUT = 30000; // request timeout ms

    @Autowired
    protected AccountService accountService;

    @Data
    public class Result {
        private boolean success;
        private ResponseEntity<?> response;
        private JSONObject object;
    }

    protected Result checkRequest(String api, String accessKey, String signature, String post) {
        Result result = new Result();

        Account account = null;
        if (accessKey != null) {
            account = accountService.getAccountByAccessKey(accessKey);
        }

        String remoteAddr = ThreadContextUtil.getRequestRemoteAddr();

        log.info("api:{}, account:{}, access-key:{}, remote-addr:{}, data:{}",
                api, account == null ? null : account.getName(), accessKey, remoteAddr, post);

        if (accessKey == null || accessKey.isEmpty()) {
            result.setResponse(createResponse(ErrorCodeEnum.MISS_AK));
            return result;
        }

        if (signature == null || signature.isEmpty()) {
            result.setResponse(createResponse(ErrorCodeEnum.MISS_SIGN));
            return result;
        }

        if (post == null || post.isEmpty()) {
            result.setResponse(createResponse(ErrorCodeEnum.POST_BODY_EMPTY));
            return result;
        }

        JSONObject object;

        try {
            object = JSON.parseObject(post);
        } catch (JSONException e) {
            log.error("Json format error: " + post, e);
            result.setResponse(createResponse(ErrorCodeEnum.FORMAT_PARSE_ERROR));
            return result;
        }

        ErrorCodeEnum check = accountService.verifySignature(post, accessKey, signature);
        if (check != ErrorCodeEnum.SUCCESS) {
            result.setResponse(createResponse(check));
            return result;
        }

        if (!object.containsKey(SSOConstants.ApiParam.TIMESTAMP)) {
            result.setResponse(createResponse(ErrorCodeEnum.MISS_TIMESTAMP));
            return result;
        }

        long timestamp = 0;

        try {
            timestamp = object.getLongValue(SSOConstants.ApiParam.TIMESTAMP);
        } catch (JSONException e) {
            log.error("Timestamp format error:" + post, e);
            result.setResponse(createResponse(ErrorCodeEnum.FORMAT_PARSE_ERROR));
            return result;
        }

        if (System.currentTimeMillis() - timestamp > REQ_TIMEOUT) {
            result.setResponse(createResponse(ErrorCodeEnum.REQ_TIMEOUT));
            return result;
        }

        result.setSuccess(true);
        result.setObject(object);
        return result;

    }

    protected ResponseEntity<Map<String, Object>> createResponse(ErrorCodeEnum error) {
        return createResponse(error, null);
    }

    protected ResponseEntity<Map<String, Object>> createResponse(ErrorCodeEnum error, Object content) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put(SSOConstants.ApiParam.STATUS, error.getHttpStatus() == HttpStatus.OK ? 0 : 1);
        result.put(SSOConstants.ApiParam.CODE, error.getErrorCode());
        result.put(SSOConstants.ApiParam.MESSAGE, error.getErrorMessage());

        if (content != null) {
            result.put(SSOConstants.ApiParam.CONTENT, content);
        }
        log.info("http-code:{}, error-code:{}, error-message:{}", error.getHttpStatus().value(), error.getErrorCode(), error.getErrorMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<Map<String, Object>>(result, headers, error.getHttpStatus());
    }

}
