package com.govorovsky.webserver.server;

import com.govorovsky.webserver.http.HttpHandler;
import com.govorovsky.webserver.http.HttpStaticHandler;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Simple multithreading HTTP server which using thread pools
 * for client processing.
 */
public class GHTTPServer {

    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private static volatile int cnt = 0;
    private static final String DOCUMENT_ROOT = "./htdocs";
    private static final String signature = "GHTTP Server 0.1";
    private static HttpHandler handler;
    private final HttpWorkers httpWorkers;
    private ServerSocket socket;


    public GHTTPServer() {
        this(DEFAULT_PORT, new HttpStaticHandler(), Runtime.getRuntime().availableProcessors() + 1);
    }

    public GHTTPServer(int port, HttpHandler handler, int numWorkers) {
        this.port = port;
        GHTTPServer.handler = handler;
        httpWorkers = new HttpWorkers(numWorkers);
    }

    public void start() throws IOException {
        socket = new ServerSocket(port);
        httpWorkers.start();
        System.err.println("Server starting on port: " + port);
        new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    httpWorkers.handleClient(socket.accept());
                } catch (IOException e) {
                    return;
                }
            }
        }).start();
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpWorkers.stop();
    }

    public static String getDocumentRoot() {
        return DOCUMENT_ROOT;
    }
    public static HttpHandler getHandler() {
        return handler;
    }

    public static void setHandler(HttpHandler handler) {
        GHTTPServer.handler = handler;
    }

    public static String getSignature() {
        return signature;
    }


    public static void main(String[] args) throws IOException {
        GHTTPServer server = new GHTTPServer();
        server.start();
    }
}
