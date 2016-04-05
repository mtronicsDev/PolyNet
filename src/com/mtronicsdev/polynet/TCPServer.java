package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class TCPServer {
    private final ServerSocket serverSocket;
    private final Thread serverThread;

    private final List<TCPSocket> clientConnections;
    private boolean running = false;

    public TCPServer(int port) throws IOException {
        if (port > 65535 || port <= 0)
            throw new IllegalArgumentException("The port number (here: " + port + ") has to be between " +
                    "0 (exclusive) and 65535 (inclusive).");

        serverSocket = new ServerSocket(port);
        serverThread = new Thread(new ServerThread());
        clientConnections = new LinkedList<>();
    }

    public void start() {
        running = true;
        serverThread.start();
        System.out.println("Server started!");
    }

    public void stop() {
        serverThread.interrupt();
        clientConnections.forEach(TCPSocket::stop);
        running = false;
        System.out.println("Server stopped!");
    }

    public List<TCPSocket> getClientConnections() {
        return Collections.unmodifiableList(clientConnections);
    }

    public boolean isRunning() {
        return running;
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    //Listen for client
                    Socket clientSocket = serverSocket.accept();

                    System.out.println("New client " + clientSocket.toString() + " connected!");
                    //Create handler for new client
                    TCPSocket socket = new TCPSocket(clientSocket);
                    clientConnections.add(socket);
                    socket.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
