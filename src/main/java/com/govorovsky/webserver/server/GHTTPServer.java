package com.govorovsky.webserver.server;

import com.govorovsky.webserver.http.HttpHandler;
import com.govorovsky.webserver.http.HttpStaticHandler;

import javax.sql.rowset.serial.SerialRef;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

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
    private AsynchronousServerSocketChannel socketChannel;


    public GHTTPServer() {
        this(DEFAULT_PORT, new HttpStaticHandler(), Runtime.getRuntime().availableProcessors() + 1);
    }

    public GHTTPServer(int port, HttpHandler handler, int numWorkers) {
        this.port = port;
        GHTTPServer.handler = handler;
        serverWorkers = new ServerWorkers(numWorkers);
    }

    public void start() throws IOException {
        socketChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(8080), 100);
        serverWorkers.start();
        System.err.println("Server starting on port: " + port);
        new Thread(() -> {
            while (!socketChannel.isOpen()) {
                socketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        serverWorkers.handleClient(result);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        exc.printStackTrace();
                    }
                });
            }
        }).start();
    }

    public void stop() {
        try {
            socketChannel.close();
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
        GHTTPServer server = new GHTTPServer();
        server.start();
    }
}
