package com.govorovsky.webserver.http.entities;

import java.util.Map;

/**
 * Created by Andrew Govorovsky on 05.06.14
 */
public class HttpRequest implements HttpMessage {
    private String uri;
    private HttpMethod httpMethod;
    private HttpVersion version;
    private Map<String, String> parameters;
    private byte[] body;


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpVersion getHttpVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod method) {
        this.httpMethod = method;
    }

    @Override
    public byte[] getMessageBody() {
        return body;
    }

    @Override
    public Map<String, String> getHeaders() {
        return parameters;
    }

    public void setHeaders(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
