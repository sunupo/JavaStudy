
[ClassLoader与字节码](https://blog.csdn.net/qq_53287512/article/details/127164219)
[老大难的 Java ClassLoader 再不理解就老了](https://zhuanlan.zhihu.com/p/51374915#:~:text=BootstrapClassLoader%20%E8%B4%9F%E8%B4%A3%E5%8A%A0%E8%BD%BD%20JVM%20%E8%BF%90%E8%A1%8C%E6%97%B6%E6%A0%B8%E5%BF%83%E7%B1%BB%EF%BC%8C%E8%BF%99%E4%BA%9B%E7%B1%BB%E4%BD%8D%E4%BA%8E%20JAVA_HOME%2Flib%2Frt.jar%20%E6%96%87%E4%BB%B6%E4%B8%AD%EF%BC%8C%E6%88%91%E4%BB%AC%E5%B8%B8%E7%94%A8%E5%86%85%E7%BD%AE%E5%BA%93%20java.xxx.%2A,%E9%83%BD%E5%9C%A8%E9%87%8C%E9%9D%A2%EF%BC%8C%E6%AF%94%E5%A6%82%20java.util.%2A%E3%80%81java.io.%2A%E3%80%81java.nio.%2A%E3%80%81java.lang.%2A%20%E7%AD%89%E7%AD%89%E3%80%82%20%E8%BF%99%E4%B8%AA%20ClassLoader%20%E6%AF%94%E8%BE%83%E7%89%B9%E6%AE%8A%EF%BC%8C%E5%AE%83%E6%98%AF%E7%94%B1%20C%20%E4%BB%A3%E7%A0%81%E5%AE%9E%E7%8E%B0%E7%9A%84%EF%BC%8C%E6%88%91%E4%BB%AC%E5%B0%86%E5%AE%83%E7%A7%B0%E4%B9%8B%E4%B8%BA%E3%80%8C%E6%A0%B9%E5%8A%A0%E8%BD%BD%E5%99%A8%E3%80%8D%E3%80%82)

为什么 JDBC 驱动被加载的时候使用的是 Class.forName(String name) 而不是 Classloader.loadClass(String name)
因为JDBC 驱动是通过 DriveManager，必须在 DriverManager 中注册，如果驱动类没有被初始化，则不能注册到 DriverManager 中。
而Class.forName 加载的时候就会将Class进行解释和初始化（类加载过程：加载、解释（验证，准备，解析）、初始化）, Classloader.loadClass 默认不会解释。
ps: Class.forName 三个参数的方法可以设置是否进行解释和初始化。ClassLoader.loadClass 两个参数的方法可以设置是否解释（但是方法修饰符是Protected）


```
Class.forName vs ClassLoader.loadClass
这两个方法都可以用来加载目标类，它们之间有一个小小的区别，那就是 Class.forName() 方法可以获取原生类型的 Class，而 ClassLoader.loadClass() 则会报错。

Class<?> x = Class.forName("[I");
System.out.println(x);

x = ClassLoader.getSystemClassLoader().loadClass("[I");
System.out.println(x);

---------------------
class [I

Exception in thread "main" java.lang.ClassNotFoundException: [I
...
```

[Java Constructor.newInstance()与Class.newInstance()
](https://blog.csdn.net/word5/article/details/82416928
)
```
通过反射创建新的类示例，有两种方式：
        Class.newInstance()
        Constructor.newInstance()

        以下对两种调用方式给以比较说明：
        Class.newInstance() 只能够调用无参的构造函数，即默认的构造函数；
        Constructor.newInstance() 可以根据传入的参数，调用任意构造构造函数。
```