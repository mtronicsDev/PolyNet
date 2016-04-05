package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class UDPServer {
    private final UDPSocket serverSocket;

    private boolean running = false;

    public UDPServer(int port, int rxBufferSize) throws IOException {
        if (port > 65535 || port <= 0)
            throw new IllegalArgumentException("The port number (here: " + port + ") has to be between " +
                    "0 (exclusive) and 65535 (inclusive).");

        serverSocket = new UDPSocket(new DatagramSocket(port), rxBufferSize);
    }

    public void start() {
        running = true;
        serverSocket.start();
        System.out.println("Server started!");
    }

    public void stop() {
        serverSocket.stop();
        running = false;
        System.out.println("Server stopped!");
    }

    public void write(DatagramPacket packet) {
        serverSocket.queueSendMessage(packet);
    }

    public DatagramPacket read() {
        return serverSocket.popNextReceivedMessage();
    }

    public boolean isRunning() {
        return running;
    }
}
