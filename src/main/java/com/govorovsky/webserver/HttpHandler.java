package com.govorovsky.webserver;

import com.govorovsky.webserver.http.entities.HttpMethod;
import com.govorovsky.webserver.http.entities.HttpRequest;
import com.govorovsky.webserver.http.entities.HttpResponse;

/**
 * Created by Andrew Govorovsky on 08.09.14
 */
public abstract class HttpHandler {
    public final HttpResponse service(HttpRequest req, HttpResponse resp) {
        HttpMethod method = req.getHttpMethod();
        System.out.println(method);
        switch (method) {
            case GET:
            case HEAD:
                doGet(req, resp);
                break;
        }
        return resp;
    }

    protected abstract void doGet(HttpRequest req, HttpResponse resp);
}
