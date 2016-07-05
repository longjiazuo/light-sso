package org.light4j.sso.server.sso.system.service;

import lombok.Data;

/**
 * Created by longjiazuo on 2015/6/4.
 */
@Data
public class SSOToken {
    private String content;
    private long timestamp;
}
