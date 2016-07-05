package org.light4j.sso.common.utils;

/**
 * Created by longjiazuo on 2015/6/5.
 */
public class SSOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SSOException(String message) {
        super(message);
    }

    public SSOException(Throwable cause) {
        super(cause);
    }
}
