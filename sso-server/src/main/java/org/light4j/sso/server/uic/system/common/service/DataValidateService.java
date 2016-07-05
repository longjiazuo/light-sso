package org.light4j.sso.server.uic.system.common.service;

import org.springframework.stereotype.Component;

/**
 * Created by longjiazuo on 2014/12/25.
 */
@Component
public class DataValidateService {

    private static final CharSequence NAME_RESERVED_CHARS = " :;,><[]{}=~!#$%^&*()+"; // allow ".-_@"

    public boolean validateName(String name) {
        for (int i = 0; i < NAME_RESERVED_CHARS.length(); i++) {
            CharSequence c = NAME_RESERVED_CHARS.subSequence(i, i + 1);
            if (name.contains(c)) {
                return false;
            }
        }
        return true;
    }
}
