package com.govorovsky.webserver.http.entities;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public class HttpConstants {
    public static final String CRLF = "\r\n";
    public static final String HTTP_DELIMITER = "/";
    public static final String HTTP_CONTENT_LENGTH= "Content-Length";
    public static final String HTTP_CONTENT_TYPE= "Content-Type";
    public static final String HTTP_SERVER= "Server";
    public static final String HTTP_CONNECTION= "Connection";
    public static final String HTTP_CONNECTION_CLOSE= "close";
    public static final String HTTP_CONNECTION_KEEPALIVE= "keep-alive";
    public static final String HTTP_USER_AGENT= "User-Agent";
    public static final String HTTP_DATE = "Date";

    public static final String INDEX_PATH = "/index.html";

    public static final String HTTP_HEADER_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
}
