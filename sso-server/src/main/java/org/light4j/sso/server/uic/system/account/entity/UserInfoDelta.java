package org.light4j.sso.server.uic.system.account.entity;

import lombok.Data;

/**
 * Created by longjiazuo on 2015/7/3.
 */
@Data
public class UserInfoDelta {
    private long userId;
    private Integer status;
    private String password;
    private String misc;
    private Integer level;
    private String birthplace;
    private String location;
    private Float loanPoint;
    private Float lendPoint;
}
