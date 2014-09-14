package com.govorovsky.webserver.http;

import com.govorovsky.webserver.http.entities.*;
import com.govorovsky.webserver.server.GHTTPServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew Govorovsky on 07.06.14
 */
public class HttpStaticHandler extends HttpHandler {

    @Override
    protected void doGet(HttpRequest req, HttpResponse resp) {
        String requestedPath = req.getUri();
        Map<String, String> headers = new HashMap<>();
        resp.setHeaders(headers);
        resp.setVersion(req.getHttpVersion());
        resp.setHttpMethod(req.getHttpMethod());
        headers.put(HttpConstants.HTTP_SERVER, GHTTPServer.getSignature());

        boolean isDirectory = false;
        if (requestedPath.endsWith("/")) {
            isDirectory = true;
            requestedPath += HttpConstants.INDEX_PATH;
        }
        File requestedFile = new File(GHTTPServer.getDocumentRoot() + requestedPath);
        try {
            if (!requestedFile.getCanonicalPath().startsWith(new File(GHTTPServer.getDocumentRoot()).getCanonicalPath())) {
                resp.setCode(HttpStatusCode.HTTP_403);
                headers.put(HttpConstants.HTTP_CONTENT_LENGTH, "0");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (requestedFile.exists()) {
            if (!requestedFile.isDirectory()) {
                resp.setCode(HttpStatusCode.HTTP_200);
                resp.setRequested(requestedFile);
                String fileName = requestedFile.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                headers.put(HttpConstants.HTTP_CONTENT_TYPE, HttpMimeTypes.getMimeType(fileExtension));
                headers.put(HttpConstants.HTTP_CONTENT_LENGTH, String.valueOf(requestedFile.length()));
            } else {
                resp.setCode(HttpStatusCode.HTTP_404);
                headers.put(HttpConstants.HTTP_CONTENT_LENGTH, "0");
            }

        } else {
            if (isDirectory) {
                resp.setCode(HttpStatusCode.HTTP_403);
            } else {
                resp.setCode(HttpStatusCode.HTTP_404);
            }
            headers.put(HttpConstants.HTTP_CONTENT_LENGTH, "0");
        }
    }
}
