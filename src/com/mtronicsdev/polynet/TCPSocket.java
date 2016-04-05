package com.mtronicsdev.polynet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import static com.mtronicsdev.polynet.Utilities.bytesToInt;
import static com.mtronicsdev.polynet.Utilities.intToBytes;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
class TCPSocket {
    private final Thread rxThread;
    private final Thread txThread;

    private final List<byte[]> receiveQueue;
    private final List<byte[]> sendQueue;

    private boolean running;

    TCPSocket(Socket socket) throws IOException {
        SocketReader reader = new SocketReader(socket);
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
        System.out.println("TCP socket started!");
    }

    void stop() {
        rxThread.interrupt();
        txThread.interrupt();
        running = false;
        System.out.println("TCP socket stopped!");
    }

    private void queueReceivedMessage(byte[] message) {
        synchronized (receiveQueue) {
            receiveQueue.add(message);
            receiveQueue.notify();
        }
    }

    @SuppressWarnings("Duplicates")
    public byte[] popNextReceivedMessage() {
        byte[] message;

        synchronized (receiveQueue) {
            if (receiveQueue.size() > 0) message = receiveQueue.remove(0);
            else message = null;
            receiveQueue.notify();
        }

        return message;
    }

    public void queueSendMessage(byte[] message) {
        synchronized (sendQueue) {
            sendQueue.add(message);
            sendQueue.notify();
        }
    }

    @SuppressWarnings("Duplicates")
    private byte[] popNextSendMessage() {
        byte[] message;

        synchronized (sendQueue) {
            if (sendQueue.size() > 0) message = sendQueue.remove(0);
            else message = null;
            sendQueue.notify();
        }

        return message;
    }

    private class SocketReader implements Runnable {
        private final Socket socket;
        private final InputStream rx;

        SocketReader(Socket socket) throws IOException {
            this.socket = socket;
            rx = socket.getInputStream();
        }

        @Override
        public void run() {
            while (running && !socket.isClosed() && !socket.isInputShutdown()) {
                try {
                    byte[] msgLengthBuffer = new byte[4];
                    int bytesRead = rx.read(msgLengthBuffer);

                    if (bytesRead == 4) {
                        int msgLength = bytesToInt(msgLengthBuffer);
                        byte[] currentMessage = new byte[msgLength];

                        int cursor = 0;
                        while (cursor < msgLength) {
                            int bytesReceived = rx.read(currentMessage, cursor, msgLength - cursor);
                            if (bytesReceived == -1) return;
                            else cursor += bytesReceived;
                        }

                        if (cursor != msgLength) {
                            System.err.println("Did not receive all bytes of the TCP message! Received only "
                                    + cursor + " of " + msgLength + " bytes.");
                        } else {
                            queueReceivedMessage(currentMessage);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Reading a message at socket " + toString() + " went wrong!");
                    e.printStackTrace();
                }
            }
        }
    }

    private class SocketWriter implements Runnable {
        private final Socket socket;
        private final OutputStream tx;

        SocketWriter(Socket socket) throws IOException {
            this.socket = socket;
            tx = socket.getOutputStream();
        }

        @Override
        public void run() {
            while (running && !socket.isClosed() && !socket.isOutputShutdown()) {
                if (sendQueue.size() > 0) {
                    byte[] sendBuffer = popNextSendMessage();

                    if (sendBuffer != null) {
                        try {
                            tx.write(intToBytes(sendBuffer.length));
                            tx.write(sendBuffer);
                        } catch (IOException e) {
                            System.err.println("Sending a message at socket " + toString() + " went wrong!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
