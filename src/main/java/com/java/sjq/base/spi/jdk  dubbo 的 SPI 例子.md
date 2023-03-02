[(120条消息) SPI机制 Demo\_spi demo\_SyLucky512的博客-CSDN博客](https://blog.csdn.net/SyLucky512/article/details/113079531)
## SPI机制 Demo

SPI（service provider interface），比如你有个接口，有多个实现类，那么在系统运行的时候，到底会选择哪一个实现类呢？所以这个时候就需要SPI，需要根据指定的配置或者默认的配置来加载对应的实现类。SPI一般用在插件扩展场景比较多，你想别人自己写个插件，插到你的服务中来扩展自定的功能。如jdbc就是使用这个经典的思想。jdk和[dubbo](https://so.csdn.net/so/search?q=dubbo&spm=1001.2101.3001.7020)具体实现的机制有点小的区别，具体通过下面的demo，来讲解jdk spi 和dubbo spi基本用法。

## JDK **SPI**

1、创建maven工程，结构如下图

![](https://img-blog.csdnimg.cn/2021012412410930.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1N5THVja3k1MTI=,size_16,color_FFFFFF,t_70)

2、在src/main/java中的java/spi包下创建一个接口和三个实现类，具体如下

```
package com.sylucky.java.spi;public interface ICar {void start();void run();}
```

```
package com.sylucky.java.spi;public class AudiCar implements ICar {@Overridepublic void start() {        System.out.println("奥迪开始启动啦！");    }@Overridepublic void run() {        System.out.println("别超我，我的性能也是杠杠的哦");    }}
```

```
package com.sylucky.java.spi;public class BmwCar implements ICar {@Overridepublic void start() {        System.out.println("宝马开始启动啦！");    }@Overridepublic void run() {        System.out.println("跑在路上，超速加速爽歪歪！");    }}
```

```
package com.sylucky.java.spi;public class TeslaCar implements ICar{@Overridepublic void start() {        System.out.println("特斯拉开始启动啦！");    }@Overridepublic void run() {        System.out.println("特斯拉正在飞速奔跑中......");    }}
```

3、在src/main/resources/META-INF/services目录下新建一个文件，文件名为接口的全类名com.sylucky.java.spi.ICar

文件内容为

```
com.sylucky.java.spi.TeslaCar#com.sylucky.java.spi.BmwCar#com.sylucky.java.spi.AudiCar
```

注：#为注释，自己测试用的

4、直接用下面的测试类运行即可

```
package com.sylucky.java.spi;import java.util.Iterator;import java.util.ServiceLoader;public class CarTest {public static void main(String[] args) {        ServiceLoader<ICar> serviceLoader = ServiceLoader.load(ICar.class);        Iterator<ICar> iterator = serviceLoader.iterator();while (iterator.hasNext()) {ICar car = iterator.next();            car.start();            car.run();        }    }}
```

运行结果如下图

![](https://img-blog.csdnimg.cn/20210124124031602.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1N5THVja3k1MTI=,size_16,color_FFFFFF,t_70)

## dubbo **SPI**

**1、基本操作入上面类似，在pom文件中引用了dubbo，maven配置如下**

```
<dependencies><dependency><groupId>org.apache.dubbo</groupId><artifactId>dubbo</artifactId><version>2.7.3</version></dependency></dependencies>
```

2、在src/main/java中的dubbo/spi包下创建一个接口和三个实现类，具体如下

```
package com.sylucky.dubbo.spi;import org.apache.dubbo.common.extension.SPI;@SPI("teslaCar")public interface ICar {void start();void run();}
```

```
package com.sylucky.dubbo.spi;public class TeslaCar implements ICar {@Overridepublic void start() {        System.out.println("特斯拉开始启动啦！");    }@Overridepublic void run() {        System.out.println("特斯拉正在飞速奔跑中......");    }}
```

```
package com.sylucky.dubbo.spi;public class BmwCar implements ICar {@Overridepublic void start() {        System.out.println("宝马开始启动啦！");    }@Overridepublic void run() {        System.out.println("跑在路上，超速加速爽歪歪！");    }}
```

```
package com.sylucky.dubbo.spi;public class AudiCar implements ICar {@Overridepublic void start() {        System.out.println("奥迪开始启动啦！");    }@Overridepublic void run() {        System.out.println("别超我，我的性能也是杠杠的哦");    }}
```

3、在src/main/resources/META-INF/dubbo目录下新建一个文件，文件名为接口的全类名com.sylucky.dubbo.spi.ICar

内容如下：

```
teslaCar=com.sylucky.dubbo.spi.TeslaCaraudiCar=com.sylucky.dubbo.spi.AudiCarbmwCar=com.sylucky.dubbo.spi.BmwCar
```

4、直接用下面的测试类运行即可

```
package com.sylucky.dubbo.spi;import org.apache.dubbo.common.extension.ExtensionLoader;public class DubboSpiCarTest {public static void main(String[] args) {        ExtensionLoader<ICar> extensionLoader = ExtensionLoader.getExtensionLoader(ICar.class);ICar teslaCar = extensionLoader.getExtension("teslaCar");        teslaCar.start();        teslaCar.run();    }}
```

运行结果如下

![](https://img-blog.csdnimg.cn/20210124124944300.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1N5THVja3k1MTI=,size_16,color_FFFFFF,t_70)