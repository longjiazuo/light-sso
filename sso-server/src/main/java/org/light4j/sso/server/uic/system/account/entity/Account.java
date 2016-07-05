package org.light4j.sso.server.uic.system.account.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Account {

    private long id;
    private String name;
    private int type;
    private Date gmtCreate;
    private Date gmtModified;

}