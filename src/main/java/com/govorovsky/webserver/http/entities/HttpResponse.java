package com.govorovsky.webserver.http.entities;

import java.io.File;
import java.util.Map;

/**
 * Created by Andrew Govorovsky on 06.06.14
 */
public class HttpResponse implements HttpMessage {
    private HttpMethod httpMethod;
    private HttpVersion version;
    private Map<String, String> headers;
    private HttpStatusCode code;
    private byte[] data;
    private File requested;

    public HttpStatusCode getCode() {
        return code;
    }

    public void setCode(HttpStatusCode code) {
        this.code = code;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    @Override
    public HttpVersion getHttpVersion() {
        return version;
    }

    @Override
    public byte[] getMessageBody() {
        return data;
    }

    public void setMessageBody(byte[] data) {
        this.data = data;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public File getRequested() {
        return requested;
    }

    public void setRequested(File requested) {
        this.requested = requested;
    }


    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }
}
