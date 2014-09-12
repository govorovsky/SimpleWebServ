package com.govorovsky.webserver.http.entities;

import java.util.Map;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public interface HttpMessage {
    HttpVersion getHttpVersion();
    HttpMethod getHttpMethod();
    byte[] getMessageBody();
    Map<String,String> getHeaders();
}
