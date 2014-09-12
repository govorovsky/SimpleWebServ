package com.govorovsky.webserver.http.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public enum HttpStatusCode {
    HTTP_200(200, "OK"),
    HTTP_400(400, "Bad Request"),
    HTTP_403(403, "Forbidden"),
    HTTP_404(404, "Not Found"),
    HTTP_405(405, "Method Not Allowed"),
    HTTP_505(505, "HTTP Version Not Supported");

    private final int code;
    private final String status;

    private int getCode() {
        return code;
    }

    HttpStatusCode(int code, String msg) {
        this.status = msg;
        this.code = code;
    }

    private static final Map<Integer, HttpStatusCode> codeToStatus = new HashMap<>();
    static {
        for(HttpStatusCode code : HttpStatusCode.values()) {
            codeToStatus.put(code.getCode(),code);
        }
    }
    public static HttpStatusCode getHttpStatusCode(int code) {
        return codeToStatus.get(code);
    }

    @Override
    public String toString() {
        return code + " " + status;
    }
}
