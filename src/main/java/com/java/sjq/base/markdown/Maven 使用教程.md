[(125条消息) Maven 最全教程，看了必懂，99% 的人都收藏了！\_Java小咖秀的博客-CSDN博客](https://blog.csdn.net/qq_14958051/article/details/124601351)

来源：cnblogs.com/hzg110/p/6936101.html  

-   **一、为什么使用Maven这样的构建工具【why】**
    
-   **二、maven是什么【what】**
    
-   **三、安装maven**
    
-   **四、第一个maven**
    
-   **五、仓库和坐标**
    
-   **六、依赖**
    
-   **七、生命周期**
    
-   **八、Eclipse中使用maven**
    
-   **九、maven工程的依赖高级特性**
    
-   **十、build配置**
    

![93b33af913ac6f766d9a7d5bbb8a441f.png](assets/93b33af913ac6f766d9a7d5bbb8a441f.png)

图片

___

前言：目前所有的项目都在使用maven，可是一直没有时间去整理学习，这两天正好有时间，好好的整理一下。

## 一、为什么使用Maven这样的构建工具【why】

**① 一个项目就是一个工程**

如果项目非常庞大，就不适合使用package来划分模块，最好是每一个模块对应一个工程，利于分工协作。借助于maven就可以将一个项目拆分成多个工程

**② 项目中使用jar包，需要“复制”、“粘贴”项目的lib中**

同样的jar包重复的出现在不同的项目工程中，你需要做不停的复制粘贴的重复工作。借助于maven，可以将jar包保存在“仓库”中，不管在哪个项目只要使用引用即可就行。

**③ jar包需要的时候每次都要自己准备好或到官网下载**

借助于maven我们可以使用统一的规范方式下载jar包，规范

**④ jar包版本不一致的风险**

不同的项目在使用jar包的时候，有可能会导致各个项目的jar包版本不一致，导致未执行错误。借助于maven，所有的jar包都放在“仓库”中，所有的项目都使用仓库的一份jar包。

**⑤ 一个jar包依赖其他的jar包需要自己手动的加入到项目中**

FileUpload组件->IO组件，[commons-fileupload](https://so.csdn.net/so/search?q=commons-fileupload&spm=1001.2101.3001.7020)\-1.3.jar依赖于commons-io-2.0.1.jar

极大的浪费了我们导入包的时间成本，也极大的增加了学习成本。借助于maven，它会自动的将依赖的jar包导入进来。

## 二、maven是什么【what】

**① maven是一款服务于java平台的自动化构建工具**

make->Ant->Maven->Gradle

名字叫法：我们可以叫妹文也可以叫麦文，但是没有叫妈文的。

**② 构建**

构建定义：把动态的Web工程经过编译得到的编译结果部署到服务器上的整个过程。

编译：java源文件\[.java\]->编译->Classz字节码文件\[.class\]

部署：最终在sevlet容器中部署的不是动态web工程，而是编译后的文件

![6f9bc33aaf625a9da919eb688ebd7b82.png](assets/6f9bc33aaf625a9da919eb688ebd7b82.png)

图片

**③ 构建的各个环节**

-   清理clean：将以前编译得到的旧文件class字节码文件删除
    
-   编译compile：将java源程序编译成class字节码文件
    
-   测试test：自动测试，自动调用junit程序
    
-   报告report：测试程序执行的结果
    
-   打包package：动态Web工程打War包，java工程打jar包
    
-   安装install：Maven特定的概念-----将打包得到的文件复制到“仓库”中的指定位置
    
-   部署deploy：将动态Web工程生成的war包复制到Servlet容器下，使其可以运行
    

## 三、[安装maven](https://so.csdn.net/so/search?q=%E5%AE%89%E8%A3%85maven&spm=1001.2101.3001.7020)

① 当前系统是否配置JAVA\_HOME的环境变量

② 下载maven，解压maven放在一个非中文无空格的路径下

③ 配置maven的相关环境变量

-   在环境变量增加M2\_HOME，路径是maven解压后的根目录
    
-   在环境变量里的path中增加maven/bin的目录
    

④ 验证：maven -v 查看maven版本

看到版本信息，恭喜你已经OK了。

![07ae1c8740a59c625f8407d0315aa2d4.png](assets/07ae1c8740a59c625f8407d0315aa2d4.png)

图片

## 四、第一个maven

**① 创建约定的目录结构** （maven工程必须按照约定的目录结构创建）

> “
>
> 根目录：工程名 |---src：源码 |---|---main:存放主程序 |---|---|---java：java源码文件 |---|---|---resource：存放框架的配置文件 |---|---test：存放测试程序 |---pop.xml：maven的核心配置文件

我们按照上面的文件夹目录结构手动创建一下，不用任何IDE环境（手动的其实最有助于我们理解maven）

![afd53b33d5514da655ac27339bed84f4.png](assets/afd53b33d5514da655ac27339bed84f4.png)

图片

### 文件内容如下

在src/main/java/com/hzg/maven目录下新建文件Hello.java，内容如下

```
package com.hzg.maven;public class Hello {  public String sayHello(String name){return "Hello "+name+"!";  }}
```

POM文件内容：

```
<?xml version="1.0" ?><project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">    <modelVersion>4.0.0</modelVersion>    <groupId>com.hzg.maven</groupId>    <artifactId>Hello</artifactId>    <version>0.0.1-SNAPSHOT</version>    <name>Hello</name>    <dependencies>        <dependency>            <groupId>junit</groupId>            <artifactId>junit</artifactId>            <version>4.0</version>            <scope>test</scope>        </dependency>    </dependencies></project>
```

**② 常用maven命令**

-   mvn clean：清理
    
-   mvn compile：编译主程序
    
-   mvn test-compile：编译测试程序
    
-   mvn test：执行测试
    
-   mvn package：打包
    
-   mvn install：安装
    

**执行maven命令必须进入到pom.xml的目录中进行执行**

**![321ad6635386cf5d4bb70e867d3333bc.png](assets/321ad6635386cf5d4bb70e867d3333bc.png)**

进入到项目的pom.xml目录之后，就可以执行啦。

**1、运行 mvn compile**

![a0f96c63a8307ae71febd917a0b47e5b.png](assets/a0f96c63a8307ae71febd917a0b47e5b.png)

图片

OK，运行完毕，你在pom.xml配置的依赖的包已经导入到仓库了，问题来了，**仓库默认的位置在哪？**

**仓库的默认位置：** c:\\Usrs\[登录当前系统的用户名\].m2\\repository

刚才执行完compile之后，之前的文件夹发生了变化

![b2f99a5f583e050797572fc85d9da6e3.png](assets/b2f99a5f583e050797572fc85d9da6e3.png)

图片

我们发现Hello项目里里多了一个target文件夹。文件夹的内容为：

![c462915404ce48fcefc2bf3c833e5361.png](assets/c462915404ce48fcefc2bf3c833e5361.png)

图片

发现target里主要存放的就是编译后的字节码文件

**2、运行mvn test-compile** ，target文件夹下面除了classes之外多了test-classes文件夹

**3、运行mvn package** ，target文件夹下面又多了一个打好的jar包

![e0c39808c49dd167a5328f28e37c4bdc.png](assets/e0c39808c49dd167a5328f28e37c4bdc.png)

图片

4、运行mvn clean，发现整个target文件夹都没了。又回到了编译之前我们手动创建的文件夹

![efe809009059c672ed0467ce2e11d73a.png](assets/efe809009059c672ed0467ce2e11d73a.png)

图片

## 五、仓库和坐标

**① pom.xml：** Project Object Model 项目对象模型。它是maven的核心配置文件，所有的构建的配置都在这里设置。

**② 坐标：** 使用下面的三个向量在仓库中唯一的定位一个maven工程

![01a74c8a657073b9c83ef0cf712ac0c0.png](assets/01a74c8a657073b9c83ef0cf712ac0c0.png)

图片

**③ maven工程的坐标与仓库中路径的关系：**

**![551b8f5c2908af00705359677110ebd0.png](assets/551b8f5c2908af00705359677110ebd0.png)**

maven坐标和仓库对应的映射关系：\[groupId\]\[artifactId\]\[version\]\[artifactId\]-\[version\].jar

去本地仓库看一下此目录：org\\springframework\\spring-core\\4.3.4.RELEASE\\spring-core-4.3.4.RELEASE.jar

果然是完全对应的（默认仓库地址上面说过了哦，不要说不知道在哪，没事下面我们再说一下仓库）

**④ 仓库**

仓库的分类：

**1、本地仓库：** 当前电脑上的仓库，路径上已经说过了哦

**2、远程仓库：**

-   私服：搭建在局域网中，一般公司都会有私服，私服一般使用nexus来搭建。具体搭建过程可以查询其他资料
    
-   中央仓库：架设在Internet上，像刚才的springframework就是在中央仓库上
    

## 六、依赖

**① maven解析依赖信息时会到本地仓库中取查找被依赖的jar包**

-   对于本地仓库中没有的会去中央仓库去查找maven坐标来获取jar包，获取到jar之后会下载到本地仓库
    
-   对于中央仓库也找不到依赖的jar包的时候，就会编译失败了
    

**② 如果依赖的是自己或者团队开发的maven工程，需要先使用install命令把被依赖的maven工程的jar包导入到本地仓库中**

举例：现在我再创建第二个maven工程HelloFriend，其中用到了第一个Hello工程里类的sayHello(String name)方法。我们在给HelloFriend项目使用 mvn compile命令进行编译的时候，会提示缺少依赖Hello的jar包。怎么办呢？

到第一个maven工程中执行 mvn install后，你再去看一下本地仓库，你会发现有了Hello项目的jar包。一旦本地仓库有了依赖的maven工程的jar包后，你再到HelloFriend项目中使用 mvn compile命令的时候，可以成功编译

**③ 依赖范围**

**![706a20f8ef811c77313635e6b3aaa531.png](assets/706a20f8ef811c77313635e6b3aaa531.png)**

_scope就是依赖的范围_

**1、compile，** 默认值，适用于所有阶段（开发、测试、部署、运行），本jar会一直存在所有阶段。

**2、provided，** 只在开发、测试阶段使用，目的是不让Servlet容器和你本地仓库的jar包冲突 。如servlet.jar。

**3、runtime，** 只在运行时使用，如JDBC驱动，适用运行和测试阶段。

**4、test，** 只在测试时使用，用于编译和运行测试代码。不会随项目发布。

**5、system，** 类似provided，需要显式提供包含依赖的jar，Maven不会在Repository中查找它。

## 七、生命周期

Maven有三套相互独立的生命周期，请注意这里说的是“三套”，而且“相互独立”，初学者容易将Maven的生命周期看成一个整体，其实不然。这三套生命周期分别是：

**① Clean Lifecycle 在进行真正的构建之前进行一些清理工作。** Clean生命周期一共包含了三个阶段：

-   pre-clean 执行一些需要在clean之前完成的工作
    
-   clean 移除所有上一次构建生成的文件
    
-   post-clean 执行一些需要在clean之后立刻完成的工作
    

**② Default Lifecycle 构建的核心部分，编译，测试，打包，部署等等。**

-   validate
    
-   generate-sources
    
-   process-sources
    
-   generate-resources
    
-   process-resources 复制并处理资源文件，至目标目录，准备打包
    
-   compile 编译项目的源代码
    
-   process-classes
    
-   generate-test-sources
    
-   process-test-sources
    
-   generate-test-resources
    
-   process-test-resources 复制并处理资源文件，至目标测试目录
    
-   test-compile 编译测试源代码
    
-   process-test-classes
    
-   test 使用合适的单元测试框架运行测试。这些测试代码不会被打包或部署
    
-   prepare-package
    
-   package 接受编译好的代码，打包成可发布的格式，如 JAR
    
-   pre-integration-test
    
-   integration-test
    
-   post-integration-test
    
-   verify
    
-   install 将包安装至本地仓库，以让其它项目依赖。
    
-   deploy 将最终的包复制到远程的仓库，以让其它开发人员与项目共享
    

那我们在Hello的项目中执行 mvn install 命令，通过日志看看中间经历了什么？

![4a76de98f59d8d79418f7ddd0f9ea77d.png](assets/4a76de98f59d8d79418f7ddd0f9ea77d.png)

图片

通过日志我们发现，其实执行mvn install，其中已经执行了compile 和 test 。

**总结：** 不论你要执行生命周期的哪一个阶段，maven都是从这个生命周期的开始执行

**插件：** 每个阶段都有插件（plugin），看上面标红的。插件的职责就是执行它对应的命令。

**③ Site Lifecycle 生成项目报告，站点，发布站点。**

-   pre-site 执行一些需要在生成站点文档之前完成的工作
    
-   site 生成项目的站点文档
    
-   post-site 执行一些需要在生成站点文档之后完成的工作，并且为部署做准备
    
-   site-deploy 将生成的站点文档部署到特定的服务器上
    

## 八、Eclipse中使用maven

**①、配置**

选择菜单windows-->preferences（参数）-->maven

选择Installations（安装），添加你自己下载并解压好的maven目录。并打上对勾 √，点击Apply（应用）

![f1a732b894f77d6c82bba2cd4b2ad0c8.png](assets/f1a732b894f77d6c82bba2cd4b2ad0c8.png)

图片

再选择User Settings目录，在User Settings中选择Browse（浏览），选择你自己maven里的conf下的settings.xml文件。

![8b7fe1fc4bd50ed9384fbc709d7bd6c6.png](assets/8b7fe1fc4bd50ed9384fbc709d7bd6c6.png)

图片

插一句：settings.xml这个配置文件，主要是配置你本地仓库的路径的。不想使用默认路径，就打开文件，加上自己的路径配置。

```
<localRepository>C:\Program Files\Java\repository</localRepository>
```

到此，maven整个的设置就OK了。

**② 使用Eclipse创建maven的Web工程**

1、选择菜单File-->new -->project，输入maven

![11801fdbb4d67ee8e527914be66ffd68.png](assets/11801fdbb4d67ee8e527914be66ffd68.png)

图片

选择Maven Project，点击Next

![80a354cb7e8d8641849f6360bd6a82ba.png](assets/80a354cb7e8d8641849f6360bd6a82ba.png)

图片

点击Next

![7976b67e7133d45dd9d988d81ecdaab3.png](assets/7976b67e7133d45dd9d988d81ecdaab3.png)

图片

输入webapp，选中第一项，点击next

![bb42e1a57fdf1a2760cfb65a05f81414.png](assets/bb42e1a57fdf1a2760cfb65a05f81414.png)

图片

项目就创建完成了，但是jdk的版本还有sevlet-api等jar包还没有

![cf97534bf496b58004fa8eaf561875c1.png](assets/cf97534bf496b58004fa8eaf561875c1.png)

图片

选择创建好的工程单击右键，选择properties 并找到 Java Build Path，把jdk的版本选择你电脑上的正确的jdk版本。

![8dce1e5089afd704aee214b5beb1b9ec.png](assets/8dce1e5089afd704aee214b5beb1b9ec.png)

图片

选择创建好的工程单击右键，选择properties 并找到 Project Facets，版本选择3.1，下面的java版本选择1.8，点击Apply

![9f759a25312797a5b14856fe0fd9becf.png](assets/9f759a25312797a5b14856fe0fd9becf.png)

图片

选择创建好的工程单击右键，找到build path

![2d7a41cd35981ac3b9fe75c481bc5cc2.png](assets/2d7a41cd35981ac3b9fe75c481bc5cc2.png)

图片

找到Libaries，添加Tomcat8.5的依赖库，点击OK

![1ac1666ecd191bd505ce56e3fa977fca.png](assets/1ac1666ecd191bd505ce56e3fa977fca.png)

图片

## 九、maven工程的依赖高级特性

**① 依赖的传递性**

**![944e83f9219093ba89b9dbe5d8536e83.png](assets/944e83f9219093ba89b9dbe5d8536e83.png)**

WebMavenDemo项目依赖JavaMavenService1 JavaMavenService1项目依赖JavaMavenService2

pom.xml文件配置好依赖关系后，必须首先mvn install后，依赖的jar包才能使用。

-   WebMavenDemo的pom.xml文件想能编译通过，JavaMavenService1必须mvn install
    
-   JavaMavenService的pom.xml文件想能编译通过，JavaMavenService2必须mvn install
    

**传递性：**

**![24928d5927740379feb803c8adea3f58.png](assets/24928d5927740379feb803c8adea3f58.png)**

在Eclipse中，为JavaMavenService2中增加了一个spring-core.jar包后，会惊喜的发现依赖的两个项目都自动的增加了这个jar包，这就是依赖的传递性。

> “
>
> 注意：非compile范围的依赖是不能传递的。

**② 依赖版本的原则：**

**1、路径最短者优先原则**

**![c4976563c71e7fd31ed0c9441cf70c2d.png](assets/c4976563c71e7fd31ed0c9441cf70c2d.png)**

Service2的log4j的版本是1.2.7版本，Service1排除了此包的依赖，自己加了一个Log4j的1.2.9的版本，那么WebMavenDemo项目遵守路径最短优先原则，Log4j的版本和Sercive1的版本一致。

**2、路径相同先声明优先原则**

**![ee14c8d54b4af4a8c7dce4eeca0c44f2.png](assets/ee14c8d54b4af4a8c7dce4eeca0c44f2.png)**

这种场景依赖关系发生了变化，WebMavenDemo项目依赖Sercive1和Service2，它俩是同一个路径，那么谁在WebMavenDemo的pom.xml中先声明的依赖就用谁的版本。

**③ 统一管理依赖的版本：**

**![002a5916976ff3d6fe35de6f1d4ec10c.png](assets/002a5916976ff3d6fe35de6f1d4ec10c.png)**

为了统一管理版本号，可以使用properties标签，里面可以自定义版本的标签名。在使用的地方使用${自定义标签名}

## 十、build配置

```
<build>  <!-- 项目的名字 -->  <finalName>WebMavenDemo</finalName>  <!-- 描述项目中资源的位置 -->  <resources>    <!-- 自定义资源1 -->    <resource>      <!-- 资源目录 -->      <directory>src/main/java</directory>      <!-- 包括哪些文件参与打包 -->      <includes>        <include>***.txt</exclude>          <exclude>**
```

配置好build后，执行mvn package之后，在maven工程指定的target目录里war包和文件都按照配置的生成了

![4dd2dff9c8e96dc17a64e277ca046b32.png](assets/4dd2dff9c8e96dc17a64e277ca046b32.png)

图片

好了，maven的所有的内容就整理完了。

最后推荐个最新最全的maven依赖项版本查询网站：

> “
>
> http://mvnrepository.com/

加小编微信，回复 40 白嫖40套 java/spring/kafka/redis/netty 教程/代码/视频 等

```
扫二维码，加我微信，回复：40 注意，不要乱回复 没错，不是机器人记得一定要等待，等待才有好东西
```
