package org.light4j.sso.server.uic.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.light4j.sso.client.SSOClient;
import org.light4j.sso.common.user.User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoFilter implements Filter {

    private List<Pattern> exclusions = new ArrayList<Pattern>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclusionStr = filterConfig.getInitParameter("EXCLUSIONS");
        if (exclusionStr != null && !exclusionStr.isEmpty()) {
            String[] inputs = exclusionStr.split(",");
            for (String input : inputs) {
                String regex = input.trim().replace("*", "(.*)").replace("?", "(.{1})");
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                if (pattern != null) {
                    exclusions.add(pattern);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String realIp = request.getHeader("X-Real-IP");
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String remoteAddr = (realIp != null ? realIp :
                (forwardedFor != null ? forwardedFor :
                        request.getRemoteAddr()));

        ThreadContextUtil.setRequestRemoteAddr(remoteAddr);

        String reqUri = request.getRequestURI();
        if (!isExcluded(reqUri)) {
            User user = SSOClient.getLoginUser(request, response);
            ThreadContextUtil.setLoginUser(user);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isExcluded(String uri) {
        for (Pattern exclusion : exclusions) {
            Matcher matcher = exclusion.matcher(uri);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}

