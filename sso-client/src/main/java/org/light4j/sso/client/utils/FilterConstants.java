package org.light4j.sso.client.utils;

/**
 * Created by longjiazuo on 2015/5/13.
 */
public class FilterConstants {
    public static final String API_VERSION_1_0 = "1.0";
    public static final String API_VERSION = API_VERSION_1_0;

    public static final String LOGIN_PAGE_URI = "/sso/login";
    public static final String LOGOUT_PAGE_URI = "/sso/logout";
    public static final String GET_USER_URI = "/api/sso/getLoginUser";
    public static final String CHECK_USER_URI = "/api/sso/checkUserExist";

    public static final String LOGIN_SCHEME = "https";
    public static final String LOGOUT_SCHEME = "http";
    public static final String USER_SCHEME = "http";

    public static final String SESSION_ATTR_USER = "demo_login_user";
    public static final String SEPARATOR = ",";

    public static class ConfigParam {
        public static final String SERVER_HOST = "SERVER_HOST";
        public static final String LOGIN_BACK = "LOGIN_BACK_URL";
        public static final String LOGOUT_URI = "LOGOUT_URI";
        public static final String EXCLUSIONS = "EXCLUSIONS";
        public static final String APP_KEY = "APP_KEY";
        public static final String APP_SECRET = "APP_SECRET";
    }

}
