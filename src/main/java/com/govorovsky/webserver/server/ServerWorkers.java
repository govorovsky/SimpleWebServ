package com.govorovsky.webserver.server;

import com.govorovsky.webserver.http.HttpSession;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Andrew Govorovsky on 11.09.14
 */
public class ServerWorkers {
    private final List<Worker> workers = new ArrayList<>();
    private final Random random = new Random();
    private int workersAmount;
    private volatile boolean running;

    public ServerWorkers(int workersAmount) {
        this.workersAmount = workersAmount;
        for (int i = 0; i < workersAmount; i++) {
            workers.add(new Worker(new LinkedBlockingQueue<>()));
        }
    }

    public synchronized void start() {
        if(running) return;
        running = true;
        int i = 0;
        for (Worker w : workers) {
            new Thread(w, "Worker " + i++).start();
        }
    }

    public synchronized void stop() {
        running = false;
        for (Worker w : workers) {
            w.stop();
        }
    }

    public void handleClient(Socket accepted) {
        workers.get(random.nextInt(workersAmount)).addClient(accepted);
    }

    private class Worker implements Runnable {
        private Thread workerThread;
        private LinkedBlockingQueue<Socket> clients;

        public Worker(LinkedBlockingQueue<Socket> queue) {
            clients = queue;
        }

        public void addClient(Socket client) {
            clients.add(client);
        }

        @Override
        public void run() {
            workerThread = Thread.currentThread();

            while (running) {
                try {
                    Socket client = clients.take();
                    if (client.isClosed()) continue;
                    client.setSoTimeout(300);
                    new HttpSession(client).run();
                    client.close();
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
