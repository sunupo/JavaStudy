[oracle 官网 对 dyn 的介绍 ](https://www.oracle.com/technical-resources/articles/javase/dyntypelang.html)

[博客对oracle对dyn介绍的翻译](https://blog.csdn.net/weixin_34586802/article/details/114616788)

JSR 292 —— 动态语言支持的下一步

JSR 292 为 JVM 引入了一个新的 Java 字节码指令，`invokedynamic`，以及一个新的方法连接机制。

方法调用的字节码指令

Java 虚拟机规范指定了 4 个字节码，用于方法调用：

◆invokevirtual

◆invokeinterface

◆invokestatic

◆invokespecial

新的 invokedynamic 指令

新的 invokedynamic 字节码指令的语法与 invokeinterface 指令类似：invokedynamic

但，它的 < method-specification> 只需指定方法名称，对描述符的唯一要求是它应引用非空对象。

invokeinterface 字节码指令差不多是这样的：invokedynamic #10;

//DynamicMethod java/lang/Object.lessThan:(Ljava/lang/Object;)

重要的是，invokedynamic 字节码指令运行动态语言的实现器(implementer)将方法调用编译为字节码，而不必指定目标的类型，该目标包含了方法、调用的返回类型或方法参数类型。这些类型对于执行指令的 JVM 不必是已知的。但如果未提供接收器的类型，JVM 如何找到该方法？毕竟，JVM 需要连接并调用真实类型上的真实方法。答案在于，JSR 292 还包含了一个新的动态类型语言的连接机制。JVM 使用新的连接机制获取所需的方法。

新的动态连接机制：`方法句柄(method handle)`

JDK 7 包含了新包，java.dyn，其中包含了与在 Java 平台中动态语言支持相关的类。其中一个类为 MethodHandle。**方法句柄是类型 java.dyn.MethodHandle 的一个简单对象，该对象包含一个 JVM 方法的匿名引用。**


[一文学会使用MethodHandle](https://blog.csdn.net/yuge1123/article/details/107444857#:~:text=MethodHandles%201%20%E6%9F%A5%E6%89%BE%E6%96%B9%E6%B3%95%EF%BC%8C%E4%BB%A5%E5%8F%8A%E5%8F%AF%E4%BB%A5%E5%B8%AE%E5%8A%A9%E5%AD%97%E6%AE%B5%E6%88%96%E8%80%85%E6%96%B9%E6%B3%95%E5%88%9B%E5%BB%BA%E7%9B%B8%E5%BA%94%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%20%28MethodHandle%29,2%20%E7%BB%84%E5%90%88%E5%99%A8%E6%96%B9%E6%B3%95%EF%BC%8C%E7%94%A8%E4%BA%8E%E5%B0%86%E7%8E%B0%E6%9C%89%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%BB%84%E5%90%88%E6%88%96%E8%BD%AC%E6%8D%A2%E6%88%90%E6%96%B0%E7%9A%84%E6%96%B9%E6%B3%95%203%20%E7%94%A8%E4%BA%8E%E5%88%9B%E5%BB%BA%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%9A%84%E5%85%B6%E4%BB%96%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95%E5%8F%AF%E4%BB%A5%E6%A8%A1%E6%8B%9F%E5%85%B6%E4%BB%96%E5%B8%B8%E8%A7%81%E7%9A%84JVM%E6%93%8D%E4%BD%9C%E6%88%96%E6%8E%A7%E5%88%B6%E6%B5%81%E6%A8%A1%E5%BC%8F)
[秒懂Java之方法句柄(MethodHandle)](https://blog.csdn.net/yuge1123/article/details/107444857#:~:text=MethodHandles%201%20%E6%9F%A5%E6%89%BE%E6%96%B9%E6%B3%95%EF%BC%8C%E4%BB%A5%E5%8F%8A%E5%8F%AF%E4%BB%A5%E5%B8%AE%E5%8A%A9%E5%AD%97%E6%AE%B5%E6%88%96%E8%80%85%E6%96%B9%E6%B3%95%E5%88%9B%E5%BB%BA%E7%9B%B8%E5%BA%94%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%20%28MethodHandle%29,2%20%E7%BB%84%E5%90%88%E5%99%A8%E6%96%B9%E6%B3%95%EF%BC%8C%E7%94%A8%E4%BA%8E%E5%B0%86%E7%8E%B0%E6%9C%89%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%BB%84%E5%90%88%E6%88%96%E8%BD%AC%E6%8D%A2%E6%88%90%E6%96%B0%E7%9A%84%E6%96%B9%E6%B3%95%203%20%E7%94%A8%E4%BA%8E%E5%88%9B%E5%BB%BA%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%9A%84%E5%85%B6%E4%BB%96%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95%E5%8F%AF%E4%BB%A5%E6%A8%A1%E6%8B%9F%E5%85%B6%E4%BB%96%E5%B8%B8%E8%A7%81%E7%9A%84JVM%E6%93%8D%E4%BD%9C%E6%88%96%E6%8E%A7%E5%88%B6%E6%B5%81%E6%A8%A1%E5%BC%8F)