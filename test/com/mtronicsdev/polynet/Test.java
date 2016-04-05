package com.mtronicsdev.polynet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class Test {
    public static void main(String... args) throws IOException {
        TCPServer server = new TCPServer(6969);
        server.start();

        TCPClient client = new TCPClient("localhost", 6969);
        client.start();

        Thread serverThread = new Thread(() -> {
            while (server.isRunning()) {
                for (TCPSocket socket : server.getClientConnections()) {
                    byte[] received = socket.popNextReceivedMessage();

                    if (received != null) {
                        String message = new String(received);
                        System.out.println("[SERVER] Received message: " + message);

                        String out = "Received " + message + "!";
                        socket.queueSendMessage(out.getBytes());
                        System.out.println("[SERVER] Queued message: " + out);
                    }
                }
            }
        });

        Thread clientRead = new Thread(() -> {
            while (client.isRunning()) {
                byte[] responseBuffer = client.getSocket().popNextReceivedMessage();
                if (responseBuffer != null) {
                    String response = new String(responseBuffer);
                    System.out.println("[CLIENT] Received message: " + response);
                }
            }
        });

        Thread clientWrite = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                while (client.isRunning()) {
                    String in = reader.readLine();

                    if (in != null) {
                        client.getSocket().queueSendMessage(in.getBytes());
                        System.out.println("[CLIENT] Queued message: " + in);
                    }
                }

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
        clientRead.start();
        clientWrite.start();

        System.out.println("Initialization ended!");
    }
}
