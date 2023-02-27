[(117条消息) Spring源码一：Spring 程序入口和xml解析\_喵咪的窝的博客-CSDN博客\_spring源码入口](https://blog.csdn.net/a1530633034/article/details/112692348)

### 文章目录

-   [一、Spring 源码下载](https://blog.csdn.net/a1530633034/article/details/112692348#Spring__8)
-   -   -   [1、git clone --branch v5.2.8.RELEASE https://gitee.com/Z201/spring-framework.git](https://blog.csdn.net/a1530633034/article/details/112692348#1git_clone_branch_v528RELEASE___09httpsgiteecomZ201springframeworkgit_9)
        -   [2、gradle 下载，gradle 要 JDK8 的版本。](https://blog.csdn.net/a1530633034/article/details/112692348#2gradle_gradle__JDK8__10)
        -   [3、到下载的 spring 源码路径执行 gradle 命令。](https://blog.csdn.net/a1530633034/article/details/112692348#3_spring__gradle__12)
        -   [4、用 idea 打开 spring 源码工程，在 idea 中安装插件 kotlin，重启 idea](https://blog.csdn.net/a1530633034/article/details/112692348#4_idea__spring__idea__kotlin_idea_16)
        -   [5、把编译好的源码导入到工程中。](https://blog.csdn.net/a1530633034/article/details/112692348#5_17)
-   [二、新创建maven项目。导入spring的jar](https://blog.csdn.net/a1530633034/article/details/112692348#mavenspringjar_18)
-   -   -   [1、Spring 中最核心的 4 个 jar](https://blog.csdn.net/a1530633034/article/details/112692348#1Spring__4__jar_19)
        -   -   -   [Spring-beans](https://blog.csdn.net/a1530633034/article/details/112692348#Springbeans_20)
                -   [Spring-core](https://blog.csdn.net/a1530633034/article/details/112692348#Springcore_21)
                -   [Spring-context](https://blog.csdn.net/a1530633034/article/details/112692348#Springcontext_22)
                -   [Spring-expression](https://blog.csdn.net/a1530633034/article/details/112692348#Springexpression_23)
        -   [2、简单点的spring项目，就需要一个jar包](https://blog.csdn.net/a1530633034/article/details/112692348#2springjar_24)
        -   [3、可加些点打印日志，lombok等依赖，自定义](https://blog.csdn.net/a1530633034/article/details/112692348#3lombok_27)
-   [三、把源码导入到工程](https://blog.csdn.net/a1530633034/article/details/112692348#_30)
-   -   -   [1、找到对接的包，右键点击Open Library Settings](https://blog.csdn.net/a1530633034/article/details/112692348#1Open_Library_Settings_31)
        -   [2、点击Classes，再点击+号， 添加导入的spring源码spring-context下libs中的jar。](https://blog.csdn.net/a1530633034/article/details/112692348#2Classes_springspringcontextlibsjar_34)
        -         [点击Sources，再点击+号，添加导入的spring源码spring-context下所有文件](https://blog.csdn.net/a1530633034/article/details/112692348#nbsp_nbsp_nbsp__Sourcesspringspringcontext_35)
-   [四、Spring 容器加载方式 （常用两种）](https://blog.csdn.net/a1530633034/article/details/112692348#Spring___38)
-   -   -   [1、类路径获取配置文件](https://blog.csdn.net/a1530633034/article/details/112692348#1_39)
        -   [2、无 配 置 文 件 加 载 容 器 （注解）](https://blog.csdn.net/a1530633034/article/details/112692348#2__________43)
-   [五、Spring 容器加载核心方法](https://blog.csdn.net/a1530633034/article/details/112692348#Spring__48)
-   [六、Xml 流程分析](https://blog.csdn.net/a1530633034/article/details/112692348#Xml__51)
-   [七、默认标签解析](https://blog.csdn.net/a1530633034/article/details/112692348#_54)
-   -   -   -   [默认标签有四个（import、alias、bean、beans）](https://blog.csdn.net/a1530633034/article/details/112692348#importaliasbeanbeans_55)
        -   [下面只使用常用的bean标签举例：](https://blog.csdn.net/a1530633034/article/details/112692348#bean_57)
        -   [1、解析document，将其中bean标签的属性BeanDefinition](https://blog.csdn.net/a1530633034/article/details/112692348#1documentbeanBeanDefinition_58)
        -   [2、注册BeanDefinition，把注册BeanDefinition放到BeanFactory的beanDefinitionMap属性中](https://blog.csdn.net/a1530633034/article/details/112692348#2BeanDefinitionBeanDefinitionBeanFactorybeanDefinitionMap_63)
-   [八、自定义标签解析](https://blog.csdn.net/a1530633034/article/details/112692348#_68)
-   -   -   [1、获取自定义标签的 namespace 命令空间：（spring.xml中beans标签xsi:schemaLocation属性值）](https://blog.csdn.net/a1530633034/article/details/112692348#1_namespace_springxmlbeansxsischemaLocation_69)
        -   [2、根据命令空间获取 NamespaceHandler 对象。](https://blog.csdn.net/a1530633034/article/details/112692348#2_NamespaceHandler__74)
        -   [3、通过反射获取spring.handlerswen文件中类实现了NamespaceHandler接口的类](https://blog.csdn.net/a1530633034/article/details/112692348#3springhandlerswenNamespaceHandler_88)
        -   [4、初始化 注册解析器存到 NamespaceHandlerSupport的parsers](https://blog.csdn.net/a1530633034/article/details/112692348#4___NamespaceHandlerSupportparsers_94)
        -   [5、获取对应的解析类，执行对应的解析方法](https://blog.csdn.net/a1530633034/article/details/112692348#5_100)
-   [八、BeanDefinition的属性](https://blog.csdn.net/a1530633034/article/details/112692348#BeanDefinition_108)

___

## 一、Spring 源码下载

### 1、git clone --branch v5.2.8.RELEASE https://[gitee](https://so.csdn.net/so/search?q=gitee&spm=1001.2101.3001.7020).com/Z201/spring-framework.git

### 2、[gradle](https://so.csdn.net/so/search?q=gradle&spm=1001.2101.3001.7020) 下载，gradle 要 JDK8 的版本。

### 3、到下载的 spring 源码路径执行 gradle 命令。

```
gradlew :spring-oxm:compileTestJava
```

### 4、用 idea 打开 spring 源码工程，在 idea 中安装插件 kotlin，重启 idea

### 5、把编译好的源码导入到工程中。

## 二、新创建maven项目。导入spring的jar

### 1、Spring 中最核心的 4 个 jar

-   ##### Spring-beans
    
-   ##### Spring-core
    
-   ##### Spring-context
    
-   ##### Spring-expression
    

### 2、简单点的spring项目，就需要一个jar包

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/20210116105140528.png)

### 3、可加些点打印日志，lombok等依赖，自定义

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/20210116105648779.png)

## 三、把源码导入到工程

### 1、找到对接的包，右键点击Open Library Settings

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/20210116101451800.png)

### 2、点击Classes，再点击+号， 添加导入的spring源码spring-context下libs中的jar。

###       点击Sources，再点击+号，添加导入的spring源码spring-context下所有文件

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/20210116102143732.png)

## 四、Spring 容器加载方式 （常用两种）

### 1、类路径获取配置文件

```
ApplicationContext applicationContext= new ClassPathXmlApplicationContext("spring.xml");
```

### 2、无 配 置 文 件 加 载 容 器 （注解）

```
ApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.own.app");
```

## 五、Spring 容器加载核心方法

**AbstractApplicationContext.refresh() 方法 refresh()方法是 spring 容器启动过程中的核心方法，spring 容器要加载必须执行该方法。**

## 六、Xml 流程分析

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/2021011611424462.png)

## 七、默认标签解析

#### 默认标签有四个（import、alias、bean、beans）

### 下面只使用常用的bean标签举例：

### 1、解析document，将其中bean标签的属性BeanDefinition

```
// 解析document   封装成BeanDefinition
BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
```

### 2、注册BeanDefinition，把注册BeanDefinition放到BeanFactory的beanDefinitionMap属性中

```
// 注册BeanDefinition  , 就是把注册BeanDefinition放到BeanFactory的beanDefinitionMap属性中
BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
```

## 八、自定义标签解析

### 1、获取自定义标签的 namespace 命令空间：（spring.xml中beans标签xsi:schemaLocation属性值）

```
// 根据自定义标签的前缀（例如：context:component-scan）  获取beans标签xsi:schemaLocation属性值中对应的uri (http://www.springframework.org/schema/context)
String namespaceUri = getNamespaceURI(ele);
```

### 2、根据命令空间获取 NamespaceHandler 对象。

**NamespaceUri 和NamespaceHandler 之间会建立一个映射，spring 会从所有的spring jar 包中扫描 spring.handlers 文件，建立映射关系。**

```
// 获取自定义标签的处理器
NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve( namespaceUri);
```

**上述resolve方法。**

```
// key spring.xml的nameSpaceUri 
// value 是META_INF/spring.handlers文件中映射的类
Map<String, Object> handlerMappings = getHandlerMappings();
Object handlerOrClassName = handlerMappings.get(namespaceUri);

```

### 3、通过反射获取spring.handlerswen文件中类实现了NamespaceHandler接口的类

```
// 通过反射获取spring.handlerswen文件中类实现了NamespaceHandler接口的类
NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
```

### 4、初始化 注册解析器存到 NamespaceHandlerSupport的parsers

```
// 初始化  注册 解析器存到 NamespaceHandlerSupport的parsers
namespaceHandler.init();
```

### 5、获取对应的解析类，执行对应的解析方法

```
// 根据自定义标签的前缀   获取对应的解析类。
BeanDefinitionParser parser = findParserForElement(element, parserContext);
// 执行对应的解析方法
return (parser != null ? parser.parse(element, parserContext) : null);
```

## 八、BeanDefinition的属性

![](Spring%20%E7%A8%8B%E5%BA%8F%E5%85%A5%E5%8F%A3%E5%92%8Cxml%E8%A7%A3%E6%9E%90.assets/20210116124612117.jpg)