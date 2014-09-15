package com.govorovsky.webserver.http;

import com.govorovsky.webserver.http.entities.*;
import com.govorovsky.webserver.http.util.HttpUtils;
import com.govorovsky.webserver.http.util.LambdaUtils;
import com.govorovsky.webserver.server.GHTTPServer;

import java.io.*;
import java.net.SocketTimeoutException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew Govorovsky on 07.09.14
 */
public class HttpSession {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private static final int BUFF_SIZE = 8192;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    public HttpSession(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }


    private int parseRequest(HttpRequest httpRequest) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            line = bufferedReader.readLine();
        } catch (SocketTimeoutException e) {
            return -1;
        }
        if (line == null) return -1; /* client closed socket */

        String tokens[] = line.split(" ");
        if (tokens.length != 3) {
            sendError(400);
            return -1;
        }

        HttpMethod method = HttpMethod.parseMethod(tokens[0]);
        if (method == null) {
            sendError(405);
            return -1;
        }
        httpRequest.setHttpMethod(method);

        HttpVersion version = HttpVersion.parseVersion(line);
        if (version == null) {
            sendError(505);
            return -1;
        }
        httpRequest.setVersion(version);

        httpRequest.setUri(HttpUtils.decodeStr(tokens[1].split("\\?")[0]));

        Map<String, String> headers = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            String params[] = line.split(": ");
            if (params.length == 2) {
                headers.put(params[0], params[1]);
            }
        }
        httpRequest.setHeaders(headers);
//        System.out.println(headers.toString());
        return 0;
    }

    private void sendError(int code) throws IOException {
        HttpResponse resp = new HttpResponse();
        resp.setVersion(HttpVersion.HTTP_1_1);
        resp.setCode(HttpStatusCode.getHttpStatusCode(code));
        resp.setHeaders(new HashMap<>());
        resp.getHeaders().put(HttpConstants.HTTP_CONTENT_LENGTH, String.valueOf(0));
        sendResponse(resp);
    }

    private void sendResponse(HttpResponse resp) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write(resp.getHttpVersion().toString() + ' ' + resp.getCode().toString() + HttpConstants.CRLF);
        bufferedWriter.write(HttpConstants.HTTP_DATE + ' ' + ZonedDateTime.now(ZoneId.of("GMT")).format(dateTimeFormatter) + HttpConstants.CRLF);
        Map<String, String> headers;
        if ((headers = resp.getHeaders()) != null) {
            headers.forEach(LambdaUtils.wrap((k, v) -> bufferedWriter.write(k + ": " + v + HttpConstants.CRLF)));
        }
        bufferedWriter.write(HttpConstants.HTTP_CONNECTION + ": " + HttpConstants.HTTP_CONNECTION_CLOSE + HttpConstants.CRLF + HttpConstants.CRLF);
        bufferedWriter.flush();
        if (resp.getRequested() != null && resp.getHttpMethod() != HttpMethod.HEAD) {
            serveFile(resp, outputStream);
        }
    }

    private void serveFile(HttpResponse response, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        try (InputStream is = new FileInputStream(response.getRequested())) {
            byte[] buffer = new byte[BUFF_SIZE];
            while (is.available() > 0) {
                int read = is.read(buffer);
                bufferedOutputStream.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            bufferedOutputStream.flush();
        } catch (IOException e) {
            /* ignore */
        }
    }

    public int run() throws IOException {
        HttpRequest request = new HttpRequest();
        HttpResponse response = new HttpResponse();
        if (parseRequest(request) == -1) {
            return -1;
        }
        HttpHandler httpHandler = GHTTPServer.getHandler();
        httpHandler.service(request, response);
        sendResponse(response);
        return 0;
    }
}
