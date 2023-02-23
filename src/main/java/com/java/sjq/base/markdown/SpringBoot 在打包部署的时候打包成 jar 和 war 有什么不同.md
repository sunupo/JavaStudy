[【423期】面试官：SpringBoot 在打包部署的时候打包成 jar 和 war 有什么不同?](https://mp.weixin.qq.com/s/laVMZj9v7GjQfwLbIq4wNw)
首先给大家来讲一个我们遇到的一个奇怪的问题:

我的一个springboot项目，用mvn install打包成jar，换一台有jdk的机器就直接可以用java -jar 项目名.jar的方式运行，没任何问题，为什么这里不需要tomcat也可以运行了？

然后我打包成war放进tomcat运行，发现端口号变成tomcat默认的8080（我在server.port中设置端口8090）项目名称也必须加上了。

也就是说我在原来的机器的IDEA中运行，项目接口地址为 ip:8090/listall,打包放进另一台机器的tomcat就变成了ip:8080/项目名/listall。这又是为什么呢？

通过jar运行实际上是启动了内置的tomcat,所以用的是应用的配置文件中的端口

直接部署到tomcat之后，内置的tomcat就不会启用，所以相关配置就以安装的tomcat为准，与应用的配置文件就没有关系了

哎，现在学编程的基本都不会教历史了，也没人有兴趣去钻研。

总体来说吧，很多年前，Sun 还在世的那个年代，在度过了早期用 C++写 Html 解析器的蛮荒时期后，有一批最早的脚本程序进入了 cgi 时代，此时的 Sun 决定进军这个领域，为了以示区别并显得自己高大上，于是研发了 servlet 标准，搞出了最早的 jsp。并给自己起了个高大上的称号 JavaEE （ Java 企业级应用标准，其实不就是一堆服务器以 http 提供服务吗，吹逼）。

既然是企业级标准那自然得有自己的服务器标准。于是 Servlet 标准诞生，以此标准实现的服务器称为 Servle 容器服务器，Tomcat 就是其中代表，被 Sun 捐献给了 Apache 基金会，那个时候的 Web 服务器还是个高大上的概念，当时的 Java Web 程序的标准就是 War 包(其实就是个 Zip 包)，这就是 War 包的由来。

后来随着服务器领域的屡次进化，人们发现我们为什么要这么笨重的 Web 服务器，还要实现一大堆 Servlet 之外的管理功能，简化一下抽出核心概念 servlet 不是更好吗，最早这么干的似乎是 Jetty，出现了可以内嵌的 Servelet 服务器。

去掉了一大堆非核心功能。后来 tomcat 也跟进了，再后来，本来很笨重的传统 JavaEE 服务器 Jboss 也搞了个 undertow 来凑热闹。正好这个时候微服务的概念兴起，“ use Jar，not War ”。要求淘汰传统 Servlet 服务器的呼声就起来了。

jar包和war包的区别
1、war是一个web模块，其中需要包括WEB-INF，是可以直接运行的WEB模块；jar一般只是包括一些class文件，在声明了Main_class之后是可以用java命令运行的。

2、war包是做好一个web应用后，通常是网站，打成包部署到容器中；jar包通常是开发时要引用通用类，打成包便于存放管理。

3、war是Sun提出的一种Web应用程序格式，也是许多文件的一个压缩包。这个包中的文件按一定目录结构来组织；classes目录下则包含编译好的Servlet类和Jsp或Servlet所依赖的其它类（如JavaBean）可以打包成jar放到WEB-INF下的lib目录下。

JAR文件格式以流行的ZIP文件格式为基础。与ZIP文件不同的是，JAR 文件不仅用于压缩和发布，而且还用于部署和封装库、组件和插件程序，并可被像编译器和 JVM 这样的工具直接使用。

【格式特点】：
安全性　可以对 JAR 文件内容加上数字化签名。这样，能够识别签名的工具就可以有选择地为您授予软件安全特权，这是其他文件做不到的，它还可以检测代码是否被篡改过。

减少下载时间　如果一个 applet 捆绑到一个 JAR 文件中，那么浏览器就可以在一个 HTTP 事务中下载这个 applet 的类文件和相关的资源，而不是对每一个文件打开一个新连接。

压缩 JAR 格式允许您压缩文件以提高存储效率。

传输平台扩展 Java 扩展框架（Java Extensions Framework）提供了向 Java 核心平台添加功能的方法，这些扩展是用 JAR 文件打包的（Java 3D 和 JavaMail 就是由 Sun 开发的扩展例子）。

WAR文件就是一个Web应用程序，建立WAR文件，就是把整个Web应用程序（不包括Web应用程序层次结构的根目录）压缩起来，指定一个war扩展名。

【建立的条件】：
需要建立正确的Web应用程序的目录层次结构。

建立WEB-INF子目录，并在该目录下建立classes与lib两个子目录。

将Servlet类文件放到WEB-INF\classes目录下，将Web应用程序所使用Java类库文件（即JAR文件）放到WEB-INF\lib目录下。

将JSP页面或静态HTML页面放到上下文根路径下或其子目录下。

建立META-INF目录，并在该目录下建立context.xml文件。

下面给大家讲讲怎么将springboot项目打包成jar和war

SpringBoot项目打包成jar很简单，也是SpringBoot的常用打包格式；本篇博客将SpringBoot打包成jar和war两种方式都记录下来；

先介绍将SpringBoot打包成jar包的方式：（以下示例是在idea中演示）
