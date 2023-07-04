package com.java.sjq.base.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class NioServerDemo {
    public static void main(String[] args) throws IOException {
        // 创建ServerSocketChannel对象
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定监听端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务器已启动，等待客户端连接...");

        while (true) {
            // 接收客户端连接
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println("客户端已连接，客户端地址：" + socketChannel.getRemoteAddress());
                // 创建ByteBuffer对象
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 读取客户端发送的数据
                int len = socketChannel.read(buffer);
                if (len > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("接收到客户端发送的消息：" + message);
                }
                // 向客户端发送数据
                String response = "当前时间：" + new Date();
                buffer.clear();
                buffer.put(response.getBytes());
                buffer.flip();
                socketChannel.write(buffer);
                socketChannel.close();
            }
        }
    }
}

// 以上是一个简单的Java NIO服务器端示例，它监听8888端口并接受客户端连接，读取客户端发送的数据并向客户端发送响应。