package com.govorovsky.webserver.server;

import com.govorovsky.webserver.http.HttpHandler;
import com.govorovsky.webserver.http.HttpStaticHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

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
    private final ServerWorkers serverWorkers;
    private ServerSocketChannel socket;


    public GHTTPServer() {
        this(DEFAULT_PORT, new HttpStaticHandler(), Runtime.getRuntime().availableProcessors());
    }

    public GHTTPServer(int port, HttpHandler handler, int numWorkers) {
        this.port = port;
        GHTTPServer.handler = handler;
        serverWorkers = new ServerWorkers(numWorkers);
    }

    public void start() throws IOException {
        socket = ServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", port));
        serverWorkers.start();
        System.err.println("Server starting on port: " + port);
        new Thread(() -> {
            while (socket.isOpen()) {
                try {
                    serverWorkers.handleClient(socket.accept().socket());
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
        serverWorkers.stop();
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
        new GHTTPServer().start();
//        GHTTPServer server1 = new GHTTPServer(8080, new HttpStaticHandler(), 4);
    }
}
