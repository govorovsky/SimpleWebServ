package com.govorovsky.webserver.http.entities;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public enum HttpVersion {
    HTTP_1_0(1, 0),
    HTTP_1_1(1, 1);

    private final int major;
    private final int minor;

    HttpVersion(int v1, int v2) {
        major = v1;
        minor = v2;
    }

    @Override
    public String toString() {
        return "HTTP" +
                HttpConstants.HTTP_DELIMITER + major +
                "." + minor;
    }

    public static HttpVersion parseVersion(String firstLine) {
        if (firstLine.endsWith("1.1")) return HTTP_1_1;
        if (firstLine.endsWith("1.0")) return HTTP_1_0;
        return null;
    }
}
