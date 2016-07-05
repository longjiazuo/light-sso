package org.light4j.sso.common.user;

/**
 * Created by longjiazuo on 2015/5/13.
 */
public class User {

    private long uid;
    private String name;
    private String misc;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMisc() {
        return misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }
}
