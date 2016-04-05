package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
class UDPSocket {
    private final Thread rxThread;
    private final Thread txThread;

    private final List<DatagramPacket> receiveQueue;
    private final List<DatagramPacket> sendQueue;

    private boolean running;

    UDPSocket(DatagramSocket socket, int rxBufferSize) throws IOException {
        SocketReader reader = new SocketReader(socket, rxBufferSize);
        SocketWriter writer = new SocketWriter(socket);
        rxThread = new Thread(reader);
        txThread = new Thread(writer);

        receiveQueue = new LinkedList<>();
        sendQueue = new LinkedList<>();
    }

    void start() {
        running = true;
        rxThread.start();
        txThread.start();
        System.out.println("UDP socket started!");
    }

    void stop() {
        rxThread.interrupt();
        txThread.interrupt();
        running = false;
        System.out.println("UDP socket stopped!");
    }

    private void queueReceivedMessage(DatagramPacket message) {
        synchronized (receiveQueue) {
            receiveQueue.add(message);
            receiveQueue.notify();
        }
    }

    @SuppressWarnings("Duplicates")
    DatagramPacket popNextReceivedMessage() {
        DatagramPacket message;

        synchronized (receiveQueue) {
            if (receiveQueue.size() > 0) message = receiveQueue.remove(0);
            else message = null;
            receiveQueue.notify();
        }

        return message;
    }

    void queueSendMessage(DatagramPacket message) {
        synchronized (sendQueue) {
            sendQueue.add(message);
            sendQueue.notify();
        }
    }

    @SuppressWarnings("Duplicates")
    private DatagramPacket popNextSendMessage() {
        DatagramPacket message;

        synchronized (sendQueue) {
            if (sendQueue.size() > 0) message = sendQueue.remove(0);
            else message = null;
            sendQueue.notify();
        }

        return message;
    }

    private class SocketReader implements Runnable {
        private final DatagramSocket socket;
        private int bufferSize;

        SocketReader(DatagramSocket socket, int bufferSize) throws IOException {
            this.socket = socket;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            while (running && !socket.isClosed()) {
                byte[] receiveBuffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(receiveBuffer, bufferSize);
                try {
                    socket.receive(packet);
                    queueReceivedMessage(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SocketWriter implements Runnable {
        private final DatagramSocket socket;

        SocketWriter(DatagramSocket socket) throws IOException {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (running && !socket.isClosed()) {
                DatagramPacket sendPacket = popNextSendMessage();
                if (sendPacket == null) continue;
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
