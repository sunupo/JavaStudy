package com.java.sjq.base.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClientDemo {
    public static void main(String[] args) throws Exception {
        // Open a SocketChannel
        SocketChannel socketChannel = SocketChannel.open();

        // Connect to a server
        socketChannel.connect(new InetSocketAddress("localhost", 12345));

        // Write data to the channel
        String message = "Hello, server!";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);

        // Read data from the channel
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        socketChannel.read(readBuffer);
        String response = new String(readBuffer.array()).trim();
        System.out.println("Response from server: " + response);

        // Close the channel
        socketChannel.close();
    }
}
