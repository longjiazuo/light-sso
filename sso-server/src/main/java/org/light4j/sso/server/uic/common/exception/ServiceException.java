package org.light4j.sso.server.uic.common.exception;

import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;

import lombok.Getter;

/**
 * Created by longjiazuo on 2014/12/1.
 */
public class ServiceException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Getter
    private ErrorCodeEnum error;

    public ServiceException(ErrorCodeEnum error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
