package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class UDPClient {
    private final UDPSocket socket;
    private final InetAddress hostAddress;
    private final int hostPort;

    private boolean running = false;

    public UDPClient(String hostAddress, int hostPort, int rxBufferSize) throws IOException {
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.hostPort = hostPort;
        if (hostPort > 65535 || hostPort <= 0)
            throw new IllegalArgumentException("The port number (here: " + hostPort + ") has to be between " +
                    "0 (exclusive) and 65535 (inclusive).");

        socket = new UDPSocket(new DatagramSocket(), rxBufferSize);
    }

    public void start() {
        running = true;
        socket.start();
        System.out.println("Client started!");
    }

    public void stop() {
        socket.stop();
        running = false;
        System.out.println("Client stopped!");
    }

    public void write(byte[] message) {
        socket.queueSendMessage(new DatagramPacket(message, message.length, hostAddress, hostPort));
    }

    public byte[] read() {
        DatagramPacket packet = socket.popNextReceivedMessage();
        return packet == null ? null : packet.getData();
    }

    public boolean isRunning() {
        return running;
    }
}
