package org.light4j.sso.server.uic.system.account.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SecurityCredential {

    private long id;
    private long accountId;
    private String accessKey;
    private String secretKey;
    private Date gmtCreate;
    private Date gmtModified;

}
