package com.govorovsky.webserver.http;

import com.govorovsky.webserver.http.entities.*;
import com.govorovsky.webserver.http.util.HttpUtils;
import com.govorovsky.webserver.http.util.LambdaUtils;
import com.govorovsky.webserver.server.GHTTPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew Govorovsky on 07.09.14
 */
public class HttpSession {
    private static final int HEADER_LENGTH = 8129; // The full header should fit in here ( Apache's default 8kb )
    private static final int BUFF_SIZE = 8192; // Filesystem block size
    private final AsynchronousSocketChannel socketChannel;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    public HttpSession(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }


    private int parseRequest(HttpRequest httpRequest, ByteBuffer src) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(new String(src.array())));
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
        StringBuilder builder = new StringBuilder();
        builder.append(resp.getHttpVersion().toString()).append(' ').append(resp.getCode().toString()).append(HttpConstants.CRLF);
        builder.append(HttpConstants.HTTP_DATE).append(' ').append(ZonedDateTime.now(ZoneId.of("GMT")).format(dateTimeFormatter)).append(HttpConstants.CRLF);
        Map<String, String> headers;
        if ((headers = resp.getHeaders()) != null) {
            headers.forEach(LambdaUtils.wrap((k, v) -> builder.append(k).append(": ").append(v).append(HttpConstants.CRLF)));
        }
        builder.append(HttpConstants.HTTP_CONNECTION + ": " + HttpConstants.HTTP_CONNECTION_CLOSE + HttpConstants.CRLF + HttpConstants.CRLF);
        socketChannel.write(ByteBuffer.wrap(builder.toString().getBytes()), null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (resp.getRequested() != null && resp.getHttpMethod() != HttpMethod.HEAD) {
                    serveFile(resp);
                } else {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });
    }

    private void serveFile(HttpResponse response) {
        try {
            AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(response.getRequested().toPath(), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);
            long fileLen = asynchronousFileChannel.size();
            asynchronousFileChannel.read(buffer, 0, 0L, new CompletionHandler<Integer, Long>() {
                @Override
                public void completed(Integer result, Long totalBytesRead) {
                    totalBytesRead += result;
                    buffer.flip();
//                    System.err.println(buffer.remaining());
                    socketChannel.write(buffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer result, Void a) {
                        }

                        @Override
                        public void failed(Throwable exc, Void a) {
                            exc.printStackTrace();
                        }
                    });
//                    System.err.println("readed:" + buffer.remaining());
                    if (totalBytesRead < fileLen) {
                        buffer.clear();
                        asynchronousFileChannel.read(buffer, result, totalBytesRead, this);
                    } else {
                        try {
                            socketChannel.close();
                            asynchronousFileChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, Long attachment) {
                    exc.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int run() throws IOException {
        final HttpRequest request = new HttpRequest();
        final HttpResponse response = new HttpResponse();
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        socketChannel.read(byteBuffer, request, new CompletionHandler<Integer, HttpRequest>() {
            @Override
            public void completed(Integer result, HttpRequest attachment) {
                try {
                    if (parseRequest(request, byteBuffer) == -1) {
                        return;
                    }
                    HttpHandler httpHandler = GHTTPServer.getHandler();
                    httpHandler.service(request, response);
                    sendResponse(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, HttpRequest attachment) {
                exc.printStackTrace();
            }
        });
        return 0;
    }
}
