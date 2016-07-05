package org.light4j.sso.server.uic.system.account.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfo {

    public static final int DEFAULT_STATUS = 0;
    public static final int DEFAULT_LEVEL = 0;
    public static final float DEFAULT_LOANPOINT = 1000;
    public static final float DEFAULT_LENDPOINT = 1000;

    private long userId;
    private int status;
    private String password;
    private String misc;
    private int level;
    private String birthplace;
    private String location;
    private Float loanPoint;
    private Float lendPoint;
    private Date gmtCreate;
    private Date gmtModified;

}
