package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class TCPClient {
    private final TCPSocket socket;
    private boolean running = false;

    public TCPClient(String hostAddress, int hostPort) throws IOException {
        Socket clientSocket = new Socket(hostAddress, hostPort);
        socket = new TCPSocket(clientSocket);
    }

    public void start() {
        running = true;
        socket.start();
    }

    public void stop() {
        socket.stop();
        running = false;
    }

    public void write(byte[] message) {
        socket.queueSendMessage(message);
    }

    public byte[] read() {
        return socket.popNextReceivedMessage();
    }

    public boolean isRunning() {
        return running;
    }

    public TCPSocket getSocket() {
        return socket;
    }
}
