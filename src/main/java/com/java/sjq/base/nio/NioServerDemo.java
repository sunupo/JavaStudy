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
                ByteBuffer buffer = ByteBuffer.allocate(1024);  // 1 新建
                System.out.println("1 新建："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());
                // 读取客户端发送的数据
                int len = socketChannel.read(buffer); // 2 读取
                System.out.println("2 数据写入buffer："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                if (len > 0) {
                    buffer.flip(); // 3 翻转
                    System.out.println("3 翻转："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes); //
                    System.out.println("4 从buffer读数据："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                    String message = new String(bytes, "UTF-8");
                    System.out.println("接收到客户端发送的消息：" + message);
                }
                // 向客户端发送数据
                String response = "当前时间：" + new Date();
                buffer.clear();
                System.out.println("5 clear："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());
                buffer.put(response.getBytes());
                System.out.println("6 buffer put："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                buffer.flip();
                System.out.println("7 buffer flip："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                socketChannel.write(buffer);
                System.out.println("8 buffer write："+buffer.position()+"\t"+buffer.limit()+"\t"+buffer.capacity());

                socketChannel.close();
            }
        }
    }
}

// 以上是一个简单的Java NIO服务器端示例，它监听8888端口并接受客户端连接，读取客户端发送的数据并向客户端发送响应。

/**
 * output:
 * 服务器已启动，等待客户端连接...
 * 客户端已连接，客户端地址：/127.0.0.1:53450
 * 1 新建：0	1024	1024
 * 2 数据写入buffer：14	1024	1024
 * 3 翻转：0	14	1024
 * 4 从buffer读数据：14	14	1024
 * 接收到客户端发送的消息：Hello, server!
 * 5 clear：0	1024	1024
 * 6 buffer put：43	1024	1024
 * 7 buffer flip：0	43	1024
 * 8 buffer write：43	43	1024
 */