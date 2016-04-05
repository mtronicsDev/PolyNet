package com.mtronicsdev.polynet;

import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class Test2 {
    public static void main(String... args) throws IOException, ClassNotFoundException, InterruptedException {
        TCPServer server = new TCPServer(9696);
        server.start();

        TCPClient client = new TCPClient("localhost", 9696);
        client.start();

        BufferedImage image = ImageIO.read(new File("testimage.jpg"));
        ImageIcon wrapper = new ImageIcon(image);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(wrapper);
        client.getSocket().queueSendMessage(bytes.toByteArray());

        byte[] received;
        while (true) {
            received = server.getClientConnections().stream().findAny().get().popNextReceivedMessage();
            if (received == null) continue;
            ByteArrayInputStream inBytes = new ByteArrayInputStream(received);
            ObjectInputStream inputStream = new ObjectInputStream(inBytes);
            ImageIcon receivedWrapper = (ImageIcon) inputStream.readObject();
            ToolkitImage receivedImage = (ToolkitImage) receivedWrapper.getImage();
            ImageIO.write(receivedImage.getBufferedImage(), "JPG", new File("testimage_rec.jpg"));
            client.stop();
            server.stop();
            return;
        }
    }
}
