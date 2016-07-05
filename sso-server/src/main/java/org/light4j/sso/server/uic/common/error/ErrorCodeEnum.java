package org.light4j.sso.server.uic.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCodeEnum {

    SUCCESS(HttpStatus.OK, 0, "Success"),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, 40000, "Bad Request"),
    REQ_TIMEOUT(HttpStatus.BAD_REQUEST, 40001, "Request timeout"),
    MISS_AK(HttpStatus.BAD_REQUEST, 40002, "Missing AccessKey"),
    MISS_SIGN(HttpStatus.BAD_REQUEST, 40003, "Missing signature"),
    MISS_PARAMS(HttpStatus.BAD_REQUEST, 40004, "Missing required parameters"),
    MISS_TIMESTAMP(HttpStatus.BAD_REQUEST, 40005, "Missing timestamp"),
    INVALID_AK(HttpStatus.BAD_REQUEST, 40006, "Invalid AccessKey"),
    INVALID_SIGN(HttpStatus.BAD_REQUEST, 40007, "Invalid signature"),
    POST_BODY_EMPTY(HttpStatus.BAD_REQUEST, 40008, "Post request body is empty"),
    FORMAT_PARSE_ERROR(HttpStatus.BAD_REQUEST, 40009, "JSON format parse error"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40100, "Unauthorized"),
    API_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40101, "API not authorized"),
    OPERATION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 40102, "Operation not authorized"),
    ADMIN_EXIST(HttpStatus.UNAUTHORIZED, 40103, "Administrator already exist"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "Internal Server Error"),
    METHOD_UNSUPPORTED(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "Method unsupported"),
    INSERT_AK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 50005, "AccessKey insert failed"),

    ACCOUNT_TAKEN(HttpStatus.OK, 20001, "Account name is taken"),
    CONTAIN_RESERVED_CHAR(HttpStatus.OK, 20002, "Contain reserved character"),
    USER_NOT_EXIST(HttpStatus.OK, 20003, "User does not exist"),
    AK_NUM_LIMIT_EXCEED(HttpStatus.OK, 20008, "AccessKey count limit exceeded"),
    AK_DUPLICATED(HttpStatus.OK, 20009, "AccessKey duplicated"),
    DB_DUPLICATE_KEY(HttpStatus.OK, 20010, "Unique constraint violated"),
    DB_UPDATE_CONFLICT(HttpStatus.OK, 20013, "Simultaneous update conflict"),
    INVALID_SK(HttpStatus.OK, 20019, "SecretKey is invalid"),
    INVALID_ENCRYPT(HttpStatus.OK, 20031, "Encrypted data invalid"),
    INVALID_SSO_TOKEN(HttpStatus.OK, 20047, "SSO token is invalid"),
    EXPIRED_SSO_TOKEN(HttpStatus.OK, 20048, "SSO token is expired"),
    INVALID_SSO_TICKET(HttpStatus.OK, 20049, "SSO ticket is invalid"),
    MISS_SSO_PARAMS(HttpStatus.OK, 20050, "Missing SSO parameters"),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "Method Not Allowed");


    @Getter private HttpStatus httpStatus;
    @Getter private int errorCode;
    @Getter private String errorMessage;


    ErrorCodeEnum(HttpStatus httpStatus, int errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
