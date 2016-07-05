package org.light4j.sso.server.sso.api;

import java.util.HashMap;
import java.util.Map;

import org.light4j.sso.common.msg.SSORequest;
import org.light4j.sso.common.user.User;
import org.light4j.sso.common.utils.SSOConstants;
import org.light4j.sso.server.sso.system.service.SSOService;
import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;
import org.light4j.sso.server.uic.common.exception.ServiceException;
import org.light4j.sso.server.uic.system.account.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@RestController
@RequestMapping(value = "/api/sso")
public class SSORestApiController extends RestApiBaseController {

    @Autowired
    private SSOService ssoService;

    @RequestMapping(value = {"/get_login_user", "/getLoginUser"}, method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> getLoginUser(@RequestHeader(SSOConstants.ApiParam.ACCESS_KEY) String accessKey,
                                       @RequestHeader(SSOConstants.ApiParam.SIGNATURE) String signature,
                                       @RequestBody String post) {
        Result checkResult = checkRequest("get_login_user", accessKey, signature, post);
        if (!checkResult.isSuccess()) {
            return checkResult.getResponse();
        }

        JSONObject body = checkResult.getObject();
        String version = body.getString(SSOConstants.ApiParam.VERSION);

        String encSso = body.getString(SSOConstants.ApiParam.SSO_INFO);
        if (encSso == null || encSso.isEmpty()){
            return createResponse(ErrorCodeEnum.MISS_PARAMS);
        }

        String ssoInfo = accountService.decryptByPairedSecretKey(encSso, accessKey);
        if (ssoInfo == null || ssoInfo.isEmpty()) {
            return createResponse(ErrorCodeEnum.INVALID_ENCRYPT);
        }

        SSORequest ssoRequest = null;
        try {
            ssoRequest = JSON.parseObject(ssoInfo, SSORequest.class);
        } finally {
            if (ssoRequest == null) {
                return createResponse(ErrorCodeEnum.FORMAT_PARSE_ERROR);
            }
        }

        User user;
        try {
            user = ssoService.getLoginUser(ssoRequest);
        } catch (ServiceException e) {
            return createResponse(e.getError());
        }

        String userInfo = JSON.toJSONString(user);
        String encUser = accountService.encryptByPairedSecretKey(userInfo, accessKey);

        Map<String, String> responseBody = new HashMap<String, String>();
        responseBody.put(SSOConstants.ApiParam.LOGIN_USER, encUser);

        return createResponse(ErrorCodeEnum.SUCCESS, responseBody);
    }

    @RequestMapping(value = {"/check_user_exist", "/checkUserExist"}, method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> checkUserExist(@RequestHeader(SSOConstants.ApiParam.ACCESS_KEY) String accessKey,
                                       @RequestHeader(SSOConstants.ApiParam.SIGNATURE) String signature,
                                       @RequestBody String post) {
        Result checkResult = checkRequest("check_user_exist", accessKey, signature, post);
        if (!checkResult.isSuccess()) {
            return checkResult.getResponse();
        }

        JSONObject body = checkResult.getObject();
        String version = body.getString(SSOConstants.ApiParam.VERSION);

        String name = body.getString(SSOConstants.ApiParam.USER_NAME);
        if (name == null || name.isEmpty()){
            return createResponse(ErrorCodeEnum.MISS_PARAMS);
        }

        Account user = accountService.getUser(name);

        return user != null ?
                createResponse(ErrorCodeEnum.SUCCESS) :
                createResponse(ErrorCodeEnum.USER_NOT_EXIST);
    }

}