# 一、[(116条消息) Java Instrument\_syhmilyLee的博客-CSDN博客](https://blog.csdn.net/lhm964517471/article/details/123339412)

## JVMTI

## 什么是JVMTI

`JVM Tool Interface`简称`JVMTI`是一组对外接口，通过这组接口可以实现，获取虚拟机运行状态、线程分析、监控、调试、覆盖率分析等功能。

## JVMTIAgent

### 什么是JVMTIAgent

为了使用`JVMTI`提供的对外接口，一般采用`Agent`方式来实现`JVMTI`提供的对外接口，`JVMTIAgent`类似于`c`语言的[动态库](https://so.csdn.net/so/search?q=%E5%8A%A8%E6%80%81%E5%BA%93&spm=1001.2101.3001.7020)的概念。

### 实现方式

在`Java1.5`之前实现一个`Agent`只能通过原生的`c/c++`来实现`Agent`，在`Java1.5`之后提供了`instrument`的`agent`，也叫做`JPLISAgent（Java Programming Language Instrumentation Services Agent）`专门用于`Java`方式。

### 启动方式

`Agent`有两种启动方式

-   第一种是在`jvm`启动的时候，指定`agent`程序的位置来启动。
-   另外一种方式是jvm已经在运行了，使用`attach`的方式到目标进程里面。在`java1.5`的时候只支持`jvm`启动，在`java1.6`的时候支持`attach`的方式启动，在`jvm`的`tool.jar`里面提供了工具`VirtualMachine`来帮助启动`agent`。

## Instrument

## 什么是Instrument

`Instrument`提供了为`Java`编程语言插入代码的服务，`Instrumentation`是在方法中添加字节码，以便收集使用的数据，由于这些改变是添加字节码，不会修改程序的状态或者行为。比如监视器代码、探查器、覆盖率分析器和事件记录器。

`Instrument`只是提供插入代码服务，在方法中添加字节码，至于具体的字节码操作，是由字节码操作工具来实现的，常见的字节码操作工具包括：`CGLIB`、`Javassist`、`ASM`等。

## 获取Instrumentation实例

### 指定接收类

要获取`Instrumentation`实例，首先要指定将`Instrumentation`实例传递给哪个类，有两种方式来指定传递给这个类。

-   第一种方式是在配置文件`resource\META_INF\MANIFEST.MF`中指定。

    ```
    Manifest-Version: 1.0
    Can-Redefine-Classes: true
    Can-Retransform-Classes: true
    Premain-Class: com.lee.agent.PreMainAgent
    Agent-Class: com.lee.agent.PreMainAgent
    ```

-   第二种方式是在`pom`文件中指定，本质上也是在配置`MANIFEST.MF`文件

    ```
    <plugin>
      <excutions>
        <excution>
          <archive>
          <manifestFile>
              <Premain-Class>com.lee.agent.PreMainAgent</Premain-Class>
              <Agent-Class>com.lee.agent.PreMainAgent</Agent-Class>
            </manifestFile>
          </archive>
        </excution>
      </excutions>
    </plugin>
    ```


### 指定接收方法

-   当`JVM`以指定代理类的方式启动，在这种情况下`Instrumentation`实例被传给代理类的`premain`方法；

    ```
    public static void premain(String agentArgs, Instrumentation inst);
    public static void premain(String agentArgs);
    ```

-   当`JVM`启动后，以`attach`的方式指定代理类，在这种情况下`Instrumentation`实例被传递给代理类的`agentmain`方法。

    ```
    public static void agentmain(String agentArgs, Instrumentation inst);
    public static void agentmain(String agentArgs);
    ```


## 示例代码

## 整体流程图示

![在这里插入图片描述](https://img-blog.csdnimg.cn/73b8b45be7a74927b54211575d6ace58.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAc3lobWlseUxlZQ==,size_9,color_FFFFFF,t_70,g_se,x_16#pic_center)

## 目标程序

目标程序是被操作的程序，被修改的是目标类`TargetClass`。

```
public class Demo {
    public static void main(String[] args) throws Exception {
        TargetClass targetClass = new TargetClass();
        targetClass.targetMethod();
    }
}
public class TargetClass {
    public String targetMethod() {
        System.out.println("执行测试方式");
        return "return";
    }
}
```

## Agent程序

```
public class PreMainAgent {
    /**
     * 指定agentjar包启动，Instrument实例会传递给这个方法
     */
    public static void premain(String agentArgs, Instrumentation inst){
        customLogic(inst);
    }
    /**
     * attach方法启动，Instrument实例会传递给这个方法
     */ 
    public static void agentmain(String agentArgs, Instrumentation inst){
        customLogic(inst);
    }
    private static void customLogic(Instrumentation inst){
        inst.addTransformer(new MyClassTransformer(), true);
    }
}
class MyClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        final ClassPool classPool = ClassPool.getDefault();
        CtClass clazz;
        CtMethod ctMethod;
        try {
            if ("com/lee/TargetClass".equals(className)){
                clazz = classPool.get("com.lee.TargetClass");
                ctMethod = clazz.getDeclaredMethod("targetMethod");
                ctMethod.insertBefore("System.out.println(\"****************\");");
                byte[] byteCode = clazz.toBytecode();
                clazz.detach();
                return byteCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

## 启动

-   先将`agent`项目打包成一个`jar`包，`agent.jar`

-   两种启动方式

    -   在启动目标程序的时候指定`agent`的位置：`-javaagent:jar包路径\Jagent.jar`

    -   以`attach`方式启动

        ```
        // project1启动的pid
        VirtualMachine vm = VirtualMachine.attach("1856");
        vm.loadAgent("jar包路径\Jagent.jar");
        ```