package com.govorovsky.webserver.server;

import com.govorovsky.webserver.http.HttpSession;

import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Andrew Govorovsky on 11.09.14
 */
public class ServerWorkers {
    private final LinkedBlockingQueue<AsynchronousSocketChannel> clients = new LinkedBlockingQueue<>();
    private final ConcurrentLinkedQueue<Worker> workers = new ConcurrentLinkedQueue<>();
    private int workersAmount;
    private volatile boolean running;

    public ServerWorkers(int workersAmount) {
        this.workersAmount = workersAmount;
        for (int i = 0; i < workersAmount; i++) {
            workers.add(new Worker());
        }
    }

    public void start() {
        running = true;
        int i = 0;
        for (Worker w : workers) {
            new Thread(w, "Worker " + i++).start();
        }
    }

    public void stop() {
        running = false;
        for (Worker w : workers) {
            w.stop();
        }
    }

    public void handleClient(AsynchronousSocketChannel accepted) {
        clients.add(accepted);
    }

    private class Worker implements Runnable {
        private Thread workerThread;

        @Override
        public void run() {
            workerThread = Thread.currentThread();

            while (running) {
                try {
                    AsynchronousSocketChannel client = clients.take();
                    if(!client.isOpen()) continue;
                    client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                    new HttpSession(client).run();
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + " interrupted");
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void stop() {
            workerThread.interrupt();
        }
    }
}
