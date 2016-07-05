package org.light4j.sso.common.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by longjiazuo on 2015/3/12.
 */
public class HttpClient {
    public static class HttpResult {
        final public int code;
        final public String content;

        public HttpResult(int code, String content) {
            this.code = code;
            this.content = content;
        }
    }

    public static HttpResult httpPostJson(String url, Map<String, String> properties, String content, long readTimeoutMs) throws IOException {

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Accept","application/json");

            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            OutputStream out = conn.getOutputStream();
            out.write(content.getBytes());
            out.flush();

            int respCode = conn.getResponseCode();
            InputStream in;
            if (respCode >= HttpURLConnection.HTTP_OK && respCode < HttpURLConnection.HTTP_OK + 100) {
                in = conn.getInputStream();
            }
            else{
                in = conn.getErrorStream();
            }
            StringBuilder sb = new StringBuilder("");
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return new HttpResult(respCode, sb.toString());
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    public static HttpResult httpPostUrl(String url, Map<String, String> properties, Map<String, String> paramValues,
                                     String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);
        url += (null == encodedContent) ? "" : ("?" + encodedContent);

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.connect();
            int respCode = conn.getResponseCode();
            InputStream in;
            if (respCode >= HttpURLConnection.HTTP_OK && respCode < HttpURLConnection.HTTP_OK + 100) {
                in = conn.getInputStream();
            }
            else{
                in = conn.getErrorStream();
            }
            StringBuilder sb = new StringBuilder("");
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in, encoding));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return new HttpResult(respCode, sb.toString());
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    public static HttpResult httpGet(String url, Map<String, String> properties, Map<String, String> paramValues,
                                     String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);
        url += (null == encodedContent) ? "" : ("?" + encodedContent);

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout((int) readTimeoutMs);

            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.connect();
            int respCode = conn.getResponseCode();
            InputStream in;
            if (respCode >= HttpURLConnection.HTTP_OK && respCode < HttpURLConnection.HTTP_OK + 100) {
                in = conn.getInputStream();
            }
            else{
                in = conn.getErrorStream();
            }
            StringBuilder sb = new StringBuilder("");
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in, encoding));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return new HttpResult(respCode, sb.toString());
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    private static String encodingParams(Map<String, String> paramValues, String encoding) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Map.Entry<String, String> entry : paramValues.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
        }
        return sb.toString();
    }
}
