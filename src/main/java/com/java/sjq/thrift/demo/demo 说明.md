> demo代码参考
> [(122条消息) Thrift安装配置\_thrift 安装\_右耳听风的博客-CSDN博客](https://blog.csdn.net/weixin_35720385/article/details/126101113)

> 下面的内容参考
> [(122条消息) Thrift使用教程（Java版本）-CSDN博客](https://blog.csdn.net/wuxiaopengnihao1/article/details/125955885)

# 1 Thrift协议栈
## 1.1 Server
### 1.1.1 TSimpleServer:阻塞IO

这种工作模式只有一个线程,循环监听传过来的请求并对其进行处理,处理完才能接受下一个请求,是一种阻塞式IO的实现,因为效率比较低,实际线上环境一般用不到.一般用于开发时候演示工作流程时使用.

### 1.1.2 TNonblockingServer:非阻塞IO多路复用

这种模式与TsimpleServer最大的区别就是使用NIO,也就是非阻塞是IO的方式实现IO的多路复用,它可以同时监听多个socket的变化,但因为业务处理上还是单线程模式,所以在一些业务处理比较复杂耗时的时候效率还是不高,因为多个请求任务依然需要排队一个一个进行处理.

### 1.1.3 TThreadPoolServer:阻塞式IO

这种模式引入了线程池,主线程只负责accept,即监听Socket,当有新的请求(客户端Socket)来时,就会在线程池里起一个线程来处理业务逻辑,这样在并发量比较大的时候(但不超过线程池的数量)每个请求都能及时被处理,效率比较高,但一旦并发量很大的时候(超过线程池数量),后面来的请求也只能排队等待.

### 1.1.4 TThreadedSelectorServer:半同步半异步

这是一种多线程**半同步半异步**的服务模型,是Thrift提供的最复杂最高级的服务模型,内部有**一个专门负责处理监听Socket的线程**,有**多个专门处理业务中网络IO的线程**,有**一个专门负责决定将新Socket连接分配给哪一个线程处理的起负载均衡作用的线程**,还有`一个工作线程池`。这种模型既可以响应大量并发连接的请求又可以快速对网络IO进行读写,能适配很多场景,因此是一种使用比较高频的服务模型.


## 1.2 Transport
Transport层提供了一个简单的网络读写抽象层。这使得thrift底层的transport从系统其它部分（如：序列化/反序列化）解耦。以下是一些Transport接口提供的方法：
```
open
close
read
write
flush
```

Thrift支持如下几种Transport：

- TIOStreamTransport和TSocket这两个类的结构对应着阻塞同步IO, TSocket封装了Socket接口
- TNonblockingTrasnsort，TNonblockingSocket这两个类对应着非阻塞IO
- TMemoryInputTransport封装了一个字节数组byte[]来做输入流的封装
- TMemoryBuffer使用字节数组输出流ByteArrayOutputStream做输出流的封装
- TFramedTransport则封装了TMemoryInputTransport做输入流，封装了TByteArryOutPutStream做输出流，作为内存读写缓冲区的一个封装。TFramedTransport的flush方法时，会先写4个字节的输出流的长度作为消息头，然后写消息体。和FrameBuffer的读消息对应起来。FrameBuffer对消息时，先读4个字节的长度，再读消息体
- TFastFramedTransport是内存利用率更高的一个内存读写缓存区，它使用自动增长的byte[](不够长度才new)，而不是每次都new一个byte[]，提高了内存的使用率。其他和TFramedTransport一样，flush时也会写4个字节的消息头表示消息长度。

## 1.3 Thrift支持如下几种protocols：

- TBinaryProtocol : 二进制格式.
- TCompactProtocol : 压缩格式
- TJSONProtocol : JSON格式
- TSimpleJSONProtocol : 提供JSON只写协议, 生成的文件很容易通过脚本语言解析
  等等

## 1.4 Processor
Processor封装了从输入数据流中读数据和向数据数据流中写数据的操作。读写数据流用Protocol对象表示。

与服务相关的 processor 实现**由编译器产生**。

Processor主要工作流程如下： 从连接中读取数据（使用输入protocol），将处理授权给handler（由用户实现），最后将结果写到连接上（使用输出protocol）。