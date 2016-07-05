package org.light4j.sso.server.uic.utils;

import org.light4j.sso.common.user.User;

import lombok.Data;

public class ThreadContextUtil {

    @Data
    static class ThreadDataBuffer {
        private String requestRemoteAddr;
        private User user;
    }

    private static ThreadLocal<ThreadDataBuffer> threadData = new ThreadLocal<ThreadDataBuffer>();

    public static void clear() {
        threadData.remove();
    }

    private static ThreadDataBuffer getDataBuffer() {
        ThreadDataBuffer buffer = threadData.get();
        if (buffer == null) {
            buffer = new ThreadDataBuffer();
            threadData.set(buffer);
        }
        return buffer;
    }

    public static String getRequestRemoteAddr() {
        ThreadDataBuffer buffer = threadData.get();
        if (buffer == null) {
            return null;
        }
        return buffer.getRequestRemoteAddr();
    }

    public static User getLoginUser() {
        ThreadDataBuffer buffer = threadData.get();
        if (buffer == null) {
            return null;
        }
        return buffer.getUser();
    }

    public static void setRequestRemoteAddr(String addr) {
        getDataBuffer().setRequestRemoteAddr(addr);
    }

    public static void setLoginUser(User user) {
        getDataBuffer().setUser(user);
    }
}
