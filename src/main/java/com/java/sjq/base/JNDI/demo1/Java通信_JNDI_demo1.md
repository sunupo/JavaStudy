[(120条消息) 02\_Java通信\_JNDI\_demo1\_超级小猪\_哦耶的博客-CSDN博客](https://blog.csdn.net/chaojixiaozhu/article/details/78931445)

## 02\_Java通信\_JNDI\_demo1

![](https://csdnimg.cn/release/blogv2/dist/pc/img/original.png)

[超级小猪\_哦耶](https://blog.csdn.net/chaojixiaozhu "超级小猪_哦耶") ![](https://csdnimg.cn/release/blogv2/dist/pc/img/newCurrentTime2.png) 于 2017-12-29 14:42:08 发布 ![](https://csdnimg.cn/release/blogv2/dist/pc/img/articleReadEyes2.png) 464 ![](https://csdnimg.cn/release/blogv2/dist/pc/img/tobarCollect2.png) 收藏

版权声明：本文为博主原创文章，遵循 [CC 4.0 BY-SA](http://creativecommons.org/licenses/by-sa/4.0/) 版权协议，转载请附上原文出处链接和本声明。

下面这个小demo会将一个对象绑定到一个JNDI服务器上，然后通过JNDI客户端获得这个对象。

PS：我们姑且这样叫：JNDI服务器，JNDI客户端这样理解起来可能比较简单

1.建立一个普通的Java项目

2.导入JDK


![](http://dl2.iteye.com/upload/attachment/0099/4424/2e657594-2751-3649-9e5d-edabe450ac03.jpg)

在JDK 5的rt.jar中一共找到了4种SUN自带的JNDI实现：LDAP，CORBA，RMI，DNS。  
这4种JNDI要正常运行还需要底层的相应服务。我们没有LDAP或CORBA服务器，也就无法启动这两种JNDI服务，DNS用于查域名。唯一可以在main()中启动的就是基于RMI的JNDI服务。

PS：因为只是一个理解性的demo所以没有必要那么认真，下一个demo我们将连接weblogic服务器，获得服务器上的连接，那么我们看到的会更加直观。

3.开发绑定对象

在使用RMI来绑定对象，我们的对象必须继承接口：Remote；然后由于对象要在网络中传输，所以对象还得序列化即：继承接口——Serializable

```
package com.hh.jndi2;
import java.io.Serializable;
import java.rmi.Remote;

/**
 * 
 * @title Person
 * @description 使用RMI实现JNDI对象绑定到服务器上，对象必须继承Remote。对象要在网络中传输必须要序列化，所以继承Serializable
 * @author hadoop
 * @version 
 * @copyright (c) SINOSOFT
 *
 */
public class Person implements Remote,Serializable {
/**
 * @title long
 * @description TODO
 * @author hadoop
 * @param Person.java
 * @return TODO
 * @throws 
 */
private static final long serialVersionUID = 1L;
private String name;
private String password;
public String getName() {
return name;
}
public void setName(String name) {
this.name = name;
}
public String getPassword() {
return password;
}
public void setPassword(String password) {
this.password = password;
}
public String toString(){
return "name:"+name+" password:"+password;
}
}

```

4.开发测试类

```
package com.hh.jndi2;import java.rmi.RemoteException;import java.rmi.registry.LocateRegistry;import javax.naming.Context;import javax.naming.InitialContext;import javax.naming.NamingException;import javax.naming.spi.NamingManager;public class Test {public static void initPerson() throws Exception{LocateRegistry.createRegistry(3000);System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");System.setProperty(Context.PROVIDER_URL, "rmi://localhost:3000");初始化InitialContext ctx = new InitialContext();Person p = new Person();p.setName("zc");p.setPassword("123");ctx.bind("person", p);ctx.close();}public static void findPerson() throws Exception{InitialContext ctx = new InitialContext();Person person = (Person) ctx.lookup("person");System.out.println(person.toString());ctx.close();}public static void main(String[] args) throws Exception {initPerson();findPerson();}}
```

5.demo的结构：


![](http://dl2.iteye.com/upload/attachment/0099/4432/a3f10d63-a0ce-3840-967c-1d3e9d8e578e.jpg)  
 

 6.demo运行的结果：

![](http://dl2.iteye.com/upload/attachment/0099/4434/b13a95a3-266d-37aa-bc7d-4fe3bfbd67b4.jpg)

-   [查看图片附件](https://blog.csdn.net/chaojixiaozhu/article/details/78931445#)