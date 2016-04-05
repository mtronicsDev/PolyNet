package com.mtronicsdev.polynet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class UDPTest {
    public static void main(String... args) throws IOException {
        UDPServer server = new UDPServer(2020, 9);

        UDPClient client1 = new UDPClient("localhost", 2020, 9);

        UDPClient client2 = new UDPClient("localhost", 2020, 9);

        new Thread(() -> {
            server.start();

            int count = 0;

            while (true) {
                DatagramPacket rec = server.read();
                if (rec != null) {
                    System.out.println("[SERVER] Received message from "
                            + rec.getAddress() + ":" + rec.getPort() + " (" + new String(rec.getData()) + ")");

                    byte[] sendBuffer = ("ACK#" + count++).getBytes();
                    DatagramPacket send = new DatagramPacket(sendBuffer, sendBuffer.length, rec.getAddress(), rec.getPort());
                    server.write(send);
                }
            }
        }).start();

        new Thread(() -> {
            client1.start();

            SimpleDateFormat format = new SimpleDateFormat("H:m:s");

            while (true) {
                byte[] sendBuffer = ("CLIENT1 - " + format.format(Date.from(Instant.now()))).getBytes();
                client1.write(sendBuffer);
                byte[] rec = client1.read();
                if (rec != null) System.out.println("[CLIENT1] Received " + new String(rec));
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            client2.start();

            SimpleDateFormat format = new SimpleDateFormat("H.m.s");

            while (true) {
                byte[] sendBuffer = ("CLIENT2 - " + format.format(Date.from(Instant.now()))).getBytes();
                client2.write(sendBuffer);
                byte[] rec = client2.read();
                if (rec != null) System.out.println("[CLIENT2] Received " + new String(rec));
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
