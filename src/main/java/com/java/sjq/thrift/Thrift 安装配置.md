[(122条消息) Thrift安装配置\_thrift 安装\_右耳听风的博客-CSDN博客](https://blog.csdn.net/weixin_35720385/article/details/126101113)

### 文章目录

-   [thrift的下载与安装](https://blog.csdn.net/weixin_35720385/article/details/126101113#thrift_1)
-   -   [Thrift的设计提供了以下这些特性：](https://blog.csdn.net/weixin_35720385/article/details/126101113#Thrift_7)
-   [MAC OS下thrift的下载与安装](https://blog.csdn.net/weixin_35720385/article/details/126101113#MAC_OSthrift_16)
-   [windows下thrift的下载与安装](https://blog.csdn.net/weixin_35720385/article/details/126101113#windowsthrift_29)
-   [maven依赖](https://blog.csdn.net/weixin_35720385/article/details/126101113#maven_36)
-   [基本概念](https://blog.csdn.net/weixin_35720385/article/details/126101113#_50)
-   -   [基本类型：](https://blog.csdn.net/weixin_35720385/article/details/126101113#_53)
-   -   [服务端编码基本步骤：](https://blog.csdn.net/weixin_35720385/article/details/126101113#_138)
-   [客户端编码基本步骤：](https://blog.csdn.net/weixin_35720385/article/details/126101113#_158)
-   [数据传输协议](https://blog.csdn.net/weixin_35720385/article/details/126101113#_163)
-   [Thrift支持的传输模式](https://blog.csdn.net/weixin_35720385/article/details/126101113#Thrift_173)
-   [thrift生成代码](https://blog.csdn.net/weixin_35720385/article/details/126101113#thrift_183)
-   -   [实现接口Iface](https://blog.csdn.net/weixin_35720385/article/details/126101113#Iface_203)
-   [Thrift支持的服务模型](https://blog.csdn.net/weixin_35720385/article/details/126101113#Thrift_219)
-   -   [TSimpleServer服务端&客户端Client](https://blog.csdn.net/weixin_35720385/article/details/126101113#TSimpleServerClient_235)
-   [TThreadPoolServer 服务模型](https://blog.csdn.net/weixin_35720385/article/details/126101113#TThreadPoolServer__333)
-   [TNonblockingServer &Client 服务模型](https://blog.csdn.net/weixin_35720385/article/details/126101113#TNonblockingServer_Client__371)
-   [THsHaServer服务模型](https://blog.csdn.net/weixin_35720385/article/details/126101113#THsHaServer_471)
-   [异步客户端](https://blog.csdn.net/weixin_35720385/article/details/126101113#_567)

## [thrift](https://so.csdn.net/so/search?q=thrift&spm=1001.2101.3001.7020)的下载与安装

在官网下载安装包 [https://thrift.apache.org/download](https://thrift.apache.org/download)  
Thrift是由Facebook开发的，用来进行可扩展且跨语言的服务开发。它结合了功能强大的软件堆栈和代码生成引擎。  
Thrift是一个驱动层接口，它提供了用于客户端使用多种语言实现的API。  
支持的客户端语言包括C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, and OCaml 。它的目标是为了各种流行的语言提供便利的RPC调用机制，而不需要使用那些开销巨大的方式。  
要使用Thrift，就要使用一个语言中立的服务定义文件，描述数据类型和服务接口。这个文件会被用作引擎的输入，编译器为每种支持的语言生成代码。这种静态生成的设计让它非常容易被开发者所使用，而且因为类型验证都发生在编译期而非运行期，所以代码可以很有效率地运行。

## Thrift的设计提供了以下这些特性：

1、语言无关的类型  
因为类型是使用定义文件按照语言中立的方式规定的，所以它们可以被不同的语言分享。比如，C++的结构可以和Python的字典类型相互交换数据。  
2、通用传输接口  
不论你使用的是磁盘文件、内存数据还是socket流，都可以使用同一段应用代码。  
3、协议无关  
Thrift会对数据类型进行编码和解码，可以跨协议使用。  
4、支持版本  
数据类型可以加入版本信息，来支持客户端API的更新。

## MAC OS下thrift的下载与安装

在shell里面执行:

```
brew install thrift.
```

或者下载进入根目录:

```
step1:cd thrift-0.10.0
step2:./configure
step3:make
step4:make install
```

## windows下thrift的下载与安装

到thrift官网下载exe文件，然后将文件重命名为thrift.exe,拷贝到D:\\EBOOK\\thrift目录下(或者任何目录下)，然后就可以在dos环境下使用了

```
D:\EBOOK\thrift>thrift -gen java D:\work\workspace\thriftworkspace\demo1\demoHello.thrift
```

输出的java文件默认输出到当前目录下D:\\EBOOK\\thrift\\gen-java，也可以使用-o参数指定输出路径;

## [maven依赖](https://so.csdn.net/so/search?q=maven%E4%BE%9D%E8%B5%96&spm=1001.2101.3001.7020)

创建一个Maven管理的Java项目,pom.xml中添加相关的依赖,并将Hello.java文件复制到项目中:

```
<dependency>
      <groupId>org.apache.thrift</groupId>
      <artifactId>libthrift</artifactId>
      <version>0.15.0</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-log4j12</artifactId>
      <version>1.7.5</version>
    </dependency>
```

## 基本概念

thrift通过一个**中间语言IDL**(Interface Description Language接口定义语言)来定义RPC的数据类型和接口,这些内容写在以.thrift结尾的文件中,然后通过特殊的编译器来生成不同语言的代码,以满足不同需要的开发者,比如java开发者,就可以生成java代码,c++开发者可以生成c++代码,生成的代码中不但包含目标语言的接口定义,方法,数据类型,还包含有RPC协议层和传输层的实现代码.  
[Thrift IDL示例文件](https://blog.csdn.net/weixin_35720385/article/details/126107909?csdn_share_tail=%7B%22type%22:%22blog%22,%22rType%22:%22article%22,%22rId%22:%22126107909%22,%22source%22:%22weixin_35720385%22%7D&ctrtid=VIT4C)

## 基本类型：

thrift不支持无符号的类型,无符号类型可以简单理解为不能表示负数,只能表示正数的类型,像java的基本数据类型都是有符号的类型.

**bool**：布尔值，true或false，对应Java的boolean  
**byte**：8位有符号整数，对应Java的byte  
**i16**：16位有符号整数，对应Java的short  
**i32**：32位有符号整数，对应Java的int  
**i64**：64位有符号整数，对应Java的long  
**double**：64 位浮点数，对应Java的double  
**string**：utf-8编码的字符串，对应Java的String

**结构体类型：**  
struct：定义公共的对象，类似于C语言中的结构体定义，在Java中是一个JavaBean

```
struct User {
　　1: i32 id;
　　2: string name;
　　3: double salary;
　　4: bool hasCar;
}
```

**容器类型：**  
　集合中的元素可以是除了service之外的任意类型

> list:有序列表,元素可重复 　　　  
> set:无需集合,元素不可重复 　　　  
> map<K,V>:键值对集合

list：对应Java的ArrayList  
set：对应Java的HashSet  
map：对应Java的HashMap

**异常类型：**  
exception：对应Java的Exception

```
exception RequestException {
　　1:i32 code;
　　2:string detail;
}
```

**服务类型：**  
service:对应服务的接口,内部可定义各种方法,相当于java中创建interface一样,创建的service经过代码生成命令会生成客户端,服务端的框架代码

```
service Hello{
　　string helloString(1:string s);
　　i32 helloInt(1:i32 i);
　　bool helloBoolean(1:bool b);
　　void helloVoid();
　　string helloNull();
}
```

**枚举类型**

```
enum Color{
　　　　RED,
　　　　BLUE
　　}
```

**Thrift常量**  
thrift也支持常量定义，使用const关键字:

```
const i32 MAX_RETRIES_TIME=10
const string MY_WEBSITE="https://www.apache.org/dyn/closer.cgi?path=/thrift/0.16.0/thrift-0.16.0.tar.gz" 
```

**命名空间(namespace)**  
　　　　可以理解成java中的packet,用于避免一些代码冲突,每种语言都有属于自己的命名空间的方式,比如java语言,就可以使用java语言的格式

```
namespace java com.wang.project
```

此外还有一些语言特性和关键字就不一一介绍了,比如可选参数和必选参数,required和optional,定义常量const,引入文件include等

### 服务端编码基本步骤：

**0.实现服务处理接口impl,重写接口方法.**

**1.创建TProcessor(业务处理器)**

Processor封装了从输入数据流中读数据和向数据流中写数据的操作,与服务相关的Processor是由编译器编译IDL文件产生的,它的主要工作是:从连接中读取数据,把处理交给用户实现impl,最后把结果写到连接上.

**2.创建TServerTransport()**

TServerSocket是ServerTransport的阻塞式IO的实现.它实现了监听端口的作用,accept到的Socket类型都是客户端的TSocket类型(阻塞式Socket).

**3.创建TProtocol(传输协议)**

TProtocol定义了基本的协议信息,包括传输什么数据,如何解析传输的数据.

**4.创建TServer**

根据需要选择使用不同的服务模式,代码中为了演示只是用了最简单TSimpleServer

**5.启动Server**

### 客户端编码基本步骤：

创建Transport  
创建TProtocol  
基于TTransport和TProtocol创建Client  
调用Client的相应方法

## 数据传输协议

Thrift支持多种传输协议,我们可以根据自己的需要来选择合适的类型,总体上来说,分为文本传输和二进制传输,由于二进制传输在传输速率和节省带宽上有优势,所以大部分情况下使用二进制传输是比较好的选择.

```
TBinaryProtocol: 二进制格式,是thrift的默认传输协议
TCompactProtocol: 压缩格式
TJSONProtocol: JSON格式
TSimpleJSONProtocol: 提供JSON只写协议, 生成的文件很容易通过脚本语言解析
TDebugProtocol – 使用易懂可读的文本格式进行传输，以便于debug
```

客户端和服务端的协议要一致

## Thrift支持的传输模式

Thrift封装了一层传输层来支持底层的网络通信,在Thrift中称为Transport,不仅提供open,close,flush等方法,还有一些read/write方法.

```
TSocket:阻塞式IO的Transport实现,用在客户端.
　　TServerSocket:非阻塞式Socket,用于服务器端,用于监听TSocket.
　　TNonblockingSocket:非阻塞式IO的实现
　　TMemoryInputTransport: 封装了一个字节数组byte[]来做输入流的封装
　　TFramedTransport- 同样使用非阻塞方式，按块的大小进行传输,输入流封装了TMemoryInputTransport
```

## thrift生成代码

创建Thrift文件，Hello.thrift ,内容如下：

```
namespace java com.dxz.thrift.demo
 
service  HelloWorldService {
  string sayHello(1:string username)
}
```

thrift-0.15.0.exe是官网提供的windows下编译工具，运用这个工具生成相关代码：

```
D:\EBOOK\thrift>thrift-0.15.0.exe -r -gen java D:\work\workspace\thriftworkspace\demo1\Hello.thrift
```

Mac终端进入Hello.thrift所在目录,执行命令:

```
thrift -r -gen java Hello.thrift
```

## 实现接口Iface

java代码：HelloWorldImpl.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TException;
public class HelloWorldImpl implements HelloWorldService.Iface {
    public HelloWorldImpl() {
    }
    @Override
    public String sayHello(String username) throws TException {
        return "Hi," + username + " welcome to thrift world";
    }
}
```

## Thrift支持的服务模型

**TSimpleServer:**

这种工作模式只有一个线程,循环监听传过来的请求并对其进行处理,处理完才能接受下一个请求,是一种阻塞式IO的实现,因为效率比较低,实际线上环境一般用不到.一般用于开发时候演示工作流程时使用.

**TNonblockingServer:**

这种模式与TsimpleServer最大的区别就是使用NIO,也就是非阻塞是IO的方式实现IO的多路复用,它可以同时监听多个socket的变化,但因为业务处理上还是单线程模式,所以在一些业务处理比较复杂耗时的时候效率还是不高,因为多个请求任务依然需要排队一个一个进行处理.

**TThreadPoolServer:**

这种模式引入了线程池,主线程只负责accept,即监听Socket,当有新的请求(客户端Socket)来时,就会在线程池里起一个线程来处理业务逻辑,这样在并发量比较大的时候(但不超过线程池的数量)每个请求都能及时被处理,效率比较高,但一旦并发量很大的时候(超过线程池数量),后面来的请求也只能排队等待.

**TThreadedSelectorServer:**

这是一种多线程半同步半异步的服务模型,是Thrift提供的最复杂最高级的服务模型,内部有一个专门负责处理监听Socket的线程,有多个专门处理业务中网络IO的线程,有一个专门负责决定将新Socket连接分配给哪一个线程处理的起负载均衡作用的线程,还有一个工作线程池.这种模型既可以响应大量并发连接的请求又可以快速对网络IO进行读写,**能适配很多场景,因此是一种使用比较高频的服务模型.**

## TSimpleServer服务端&客户端Client

简单的单线程服务模型，一般用于测试。  
编写服务端server代码：HelloServer.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
public class HelloServer {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld TSimpleServer start ....");
            TProcessor tprocessor = new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            //HelloWorldService.Processor<HelloWorldService.Iface> tprocessor =
            new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            // 简单的单线程服务模型，一般用于测试
            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.processor(tprocessor);
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            // tArgs.protocolFactory(new TCompactProtocol.Factory());
            // tArgs.protocolFactory(new TJSONProtocol.Factory());
            TServer server = new TSimpleServer(tArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        HelloServerDemo server = new HelloServerDemo();
        server.startServer();
    }
}

```

编写客户端Client代码：HelloClient.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
public class HelloClient {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName) {
        TTransport transport = null;
        try {
            transport = new TSocket(SERVER_IP, SERVER_PORT, TIMEOUT);
            // 协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            HelloWorldService.Client client = new HelloWorldService.Client(protocol);
            transport.open();
            String result = client.sayHello(userName);
            System.out.println("Thrify client result =: " + result);
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
    public static void main(String[] args) {
        HelloClientDemo client = new HelloClientDemo();
        client.startClient("china");
    }
}
```

先运行服务端程序，日志如下：

> HelloWorld TSimpleServer start …

再运行客户端调用程序，日志如下：

> Thrify client result =: Hi,china welcome to thrift world.

测试成功，和预期的返回信息一致。

## TThreadPoolServer 服务模型

线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。  
编写服务端代码：HelloServer2.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
public class HelloServer2 {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld TThreadPoolServer start ....");
            TProcessor tprocessor = new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);
            TThreadPoolServer.Args ttpsArgs = new TThreadPoolServer.Args(serverTransport);
            ttpsArgs.processor(tprocessor);
            ttpsArgs.protocolFactory(new TBinaryProtocol.Factory());
            // 线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。
            TServer server = new TThreadPoolServer(ttpsArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        HelloServerDemo2 server = new HelloServerDemo2();
        server.startServer();
    }
}
```

客户端Client代码和之前的一样，只要数据传输的协议一致即可，客户端测试成功，结果如下：

> Thrify client result =: Hi,china welcome to thrift world.

## TNonblockingServer &Client 服务模型

使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式。

编写服务端代码：HelloServer3.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
public class HelloServer3 {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld TNonblockingServer start ....");
            TProcessor tprocessor = new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(SERVER_PORT);
            TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(tnbSocketTransport);
            tnbArgs.processor(tprocessor);
            tnbArgs.transportFactory(new TFramedTransport.Factory());
            tnbArgs.protocolFactory(new TCompactProtocol.Factory());
            // 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
            TServer server = new TNonblockingServer(tnbArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloServerDemo3 server = new HelloServerDemo3();
        server.startServer();
    }
}
```

编写客户端代码：HelloClient3.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
public class HelloClient3 {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName) {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(SERVER_IP, SERVER_PORT, TIMEOUT));
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            HelloWorldService.Client client = new HelloWorldService.Client(protocol);
            transport.open();
            String result = client.sayHello(userName);
            System.out.println("Thrify client result =: " + result);
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloClientDemo3 client = new HelloClientDemo3();
        client.startClient("HelloClientDemo3");
    }
}

```

客户端的测试成功，结果如下：

> Thrify client result =: Hi,HelloClientDemo3 welcome to thrift world.

## THsHaServer服务模型

**半同步半异步的服务端模型**，需要指定为：TFramedTransport数据传输的方式。  
编写服务端代码：HelloServer4.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
public class HelloServer4 {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld THsHaServer start ....");
            TProcessor tprocessor = new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(SERVER_PORT);
            THsHaServer.Args thhsArgs = new THsHaServer.Args(tnbSocketTransport);
            thhsArgs.processor(tprocessor);
            thhsArgs.transportFactory(new TFramedTransport.Factory());
            thhsArgs.protocolFactory(new TBinaryProtocol.Factory());
            // 半同步半异步的服务模型
            TServer server = new THsHaServer(thhsArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloServerDemo4 server = new HelloServerDemo4();
        server.startServer();
    }
}
```

客户端代码HelloClient4.java

```
package com.dxz.thrift.demo;
import java.io.IOException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
public class HelloClientDemo4 {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName) {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(SERVER_IP, SERVER_PORT, TIMEOUT));
            // 协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            HelloWorldService.Client client = new HelloWorldService.Client(protocol);
            transport.open();
            String result = client.sayHello(userName);
            System.out.println("Thrify client result =: " + result);
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloClientDemo4 client = new HelloClientDemo4();
        client.startClient("HelloClientDemo4");
    }
}
```

> 结果：Thrify client result =: Hi,HelloClientDemo4 welcome to thrift  
> world.

## 异步客户端

编写服务端代码：HelloServer5.java

```
package com.dxz.thrift.demo;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
public class HelloServer5 {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld TNonblockingServer start ....");
            TProcessor tprocessor = new HelloWorldService.Processor<HelloWorldService.Iface>(new HelloWorldImpl());
            TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(SERVER_PORT);
            TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(tnbSocketTransport);
            tnbArgs.processor(tprocessor);
            tnbArgs.transportFactory(new TFramedTransport.Factory());
            tnbArgs.protocolFactory(new TCompactProtocol.Factory());
            // 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
            TServer server = new TNonblockingServer(tnbArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloServerDemo5 server = new HelloServerDemo5();
        server.startServer();
    }
}
```

编写客户端Client代码：HelloAsynClient.java

```
package com.dxz.thrift.demo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import com.dxz.thrift.demo.HelloWorldService.AsyncClient.sayHello_call;
public class HelloAsynClient {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName) {
        try {
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TNonblockingTransport transport = new TNonblockingSocket(SERVER_IP, SERVER_PORT, TIMEOUT);
            TProtocolFactory tprotocol = new TCompactProtocol.Factory();
            HelloWorldService.AsyncClient asyncClient = new HelloWorldService.AsyncClient(tprotocol, clientManager,
                    transport);
            System.out.println("Client start .....");
            CountDownLatch latch = new CountDownLatch(1);
            AsynCallback callBack = new AsynCallback(latch);
            System.out.println("call method sayHello start ...");
            asyncClient.sayHello(userName, callBack);
            System.out.println("call method sayHello .... end");
            boolean wait = latch.await(30, TimeUnit.SECONDS);
            System.out.println("latch.await =:" + wait);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("startClient end.");
    }
    public class AsynCallback implements AsyncMethodCallback<sayHello_call> {
        private CountDownLatch latch;
        public AsynCallback(CountDownLatch latch) {
            this.latch = latch;
        }
        @Override
        public void onComplete(sayHello_call response) {
            System.out.println("onComplete");
            try {
                // Thread.sleep(1000L * 1);
                System.out.println("AsynCall result =:" + response.getResult().toString());
            } catch (TException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }
        @Override
        public void onError(Exception exception) {
            System.out.println("onError :" + exception.getMessage());
            latch.countDown();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloAsynClientDemo client = new HelloAsynClientDemo();
        client.startClient("HelloAsynClientDemo");
    }
}
```

先运行服务程序，再运行客户端程序，测试结果如下：

```
Client start .....
call method sayHello start ...
call method sayHello .... end
onComplete
AsynCall result =:Hi,HelloAsynClientDemo welcome to thrift world.
latch.await =:true
startClient end.
```
