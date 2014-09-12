package com.govorovsky.webserver.http.entities;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public enum HttpMethod {
    GET,
    HEAD;

    public static HttpMethod parseMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
