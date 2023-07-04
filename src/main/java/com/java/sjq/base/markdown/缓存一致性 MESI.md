## 缓存一致性
`CPU缓存一致性`是指多个CPU之间共享内存时，保证每个CPU访问到的内存数据是一致的，避免出现数据不一致的情况。为了保证缓存一致性，CPU采用了一些协议，如MESI协议等。

`MESI协议`是一种CPU缓存一致性协议，用于保证多个CPU之间共享内存时，每个CPU访问到的内存数据是一致的。 MESI协议通过对缓存行状态的管理，实现了缓存一致性。
> 具体来说，MESI协议将每个缓存行分为四种状态：Modified、Exclusive、Shared和Invalid。当一个CPU修改了某个缓存行中的数据时，该缓存行的状态会从Shared或Exclusive变为Modified，表示该缓存行中的数据已被修改且尚未写回内存。当其他CPU需要访问该缓存行时，会发现该缓存行的状态为Modified，此时其他CPU会将该缓存行的数据从该CPU的缓存中无效化，然后从内存中读取最新的数据。这样就保证了多个CPU之间访问内存数据的一致性。
>


----
`volatile关键字`可以保证多线程之间对变量的可见性，即一个线程修改了某个变量的值，其他线程能够立即看到修改后的值。volatile关键字的实现原理是通过禁止CPU对该变量的缓存，每次访问该变量时都必须从内存中读取最新的值，从而保证了多线程之间对变量的可见性。因此，使用volatile关键字可以避免多线程之间出现数据不一致的情况。


## volatile 缓存可见性实现原理：
主要是通过汇编语言的lock指令，它会锁定这块内存区域的缓存（**缓存行锁定**）并回写到主内存。

```
lock add dword ptr [rsp], 0h: // 还会输出一个行号，表示修改volatile变量时源文件代码所在的行号
```
IA-32 和 Intel 64 架构软件开发手册对 lock 指令的解释：
- 会将当前处理器缓存行的数据立即写回到系统内存。
- 这个写回内存的操作会引起在其它 CPU 力缓存了该地址的数据无效（MESI）协议
- 提供内存屏障的功能，使 lock 前后指令不能重排序。

## volatile 与 synchronized
- volatile 保证可见性、有序性。
- synchronized 保证原子性。

## 半初始化对象
对象的初始化分为三个步骤：**分配内存空间、初始化对象、将对象的引用赋值给变量**。如果在第二步初始化对象时出现异常，那么对象就处于半初始化状态，即对象已经分配了内存空间，但是还没有完成初始化。在这种情况下，如果另一个线程访问该对象，就会出现数据不一致的情况。因此，为了避免出现半初始化对象，可以使用synchronized关键字或者volatile关键字来保证对象的正确初始化。${INSERT_HERE}
### 1.对象创建(初始化)的过程：[参考](https://blog.csdn.net/Smartbbbb/article/details/120937027)
以下代码是使用JClassLib插件可查看的字节码信息
```
0 new #2 <java/lang/Object>
3 dup
4 invokespecial #1 <java/lang/Object.<init> : ()V>
7 astore_1
8 return

    步骤0 new来申请内存，属性会有默认值
    步骤4 调用特殊的方法构造方法
    步骤7 建立关联，引用与对象之间
    步骤8 执行结束
```

## DCL + volatile

### DCL 实现单例模式：
```java
public class Singleton {
    private volatile static Singleton instance;
    private Singleton() {}
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```
### 为什么使用 volatile？

关键部分指令:
```text
10: monitorenter
11: getstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
14: ifnonnull     27
17: new           #3                  // class com/java/sjq/base/markdown/Singleton
20: dup
21: invokespecial #4                  // Method "<init>":()V
24: putstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
27: aload_0
28: monitorexit
```
避免指令重排序，导致 `24:` 与 `21:` 重排序，导致 instance 不为 null，但是因为没有执行 '21: invokespecial" 导致 instance 是一个半初始化对象。这个半初始化对象用于执行其它代码时会出错。（比如线程1先执行了24：导致 instance 不为 null，但是 21: invokespecial 没有执行。这个时候线程 2 发现instance 不为null，就会返回一个半初始化对象）。

下面是javap完整结果。
```text
D:\SoftWare\jdk8\bin\javap.exe -verbose C:\Users\sunupo\IdeaProjects\JavaStudy\src\main\java\com\java\sjq\base\markdown\com\java\sjq\base\markdown\Singleton.class
Classfile /C:/Users/sunupo/IdeaProjects/JavaStudy/src/main/java/com/java/sjq/base/markdown/com/java/sjq/base/markdown/Singleton.class
  Last modified 2023-4-10; size 531 bytes
  MD5 checksum 3531d99be2e4b8af6779e6e55237853c
  Compiled from "Singleton.java"
public class com.java.sjq.base.markdown.Singleton
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #5.#19         // java/lang/Object."<init>":()V
   #2 = Fieldref           #3.#20         // com/java/sjq/base/markdown/Singleton.instance:Lcom/java/sjq/base/markdown/Singleton;
   #3 = Class              #21            // com/java/sjq/base/markdown/Singleton
   #4 = Methodref          #3.#19         // com/java/sjq/base/markdown/Singleton."<init>":()V
   #5 = Class              #22            // java/lang/Object
   #6 = Utf8               instance
   #7 = Utf8               Lcom/java/sjq/base/markdown/Singleton;
   #8 = Utf8               <init>
   #9 = Utf8               ()V
  #10 = Utf8               Code
  #11 = Utf8               LineNumberTable
  #12 = Utf8               getInstance
  #13 = Utf8               ()Lcom/java/sjq/base/markdown/Singleton;
  #14 = Utf8               StackMapTable
  #15 = Class              #22            // java/lang/Object
  #16 = Class              #23            // java/lang/Throwable
  #17 = Utf8               SourceFile
  #18 = Utf8               Singleton.java
  #19 = NameAndType        #8:#9          // "<init>":()V
  #20 = NameAndType        #6:#7          // instance:Lcom/java/sjq/base/markdown/Singleton;
  #21 = Utf8               com/java/sjq/base/markdown/Singleton
  #22 = Utf8               java/lang/Object
  #23 = Utf8               java/lang/Throwable
{
  public static com.java.sjq.base.markdown.Singleton getInstance();
    descriptor: ()Lcom/java/sjq/base/markdown/Singleton;
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=0
         0: getstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
         3: ifnonnull     37
         6: ldc           #3                  // class com/java/sjq/base/markdown/Singleton
         8: dup
         9: astore_0
        10: monitorenter
        11: getstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
        14: ifnonnull     27
        17: new           #3                  // class com/java/sjq/base/markdown/Singleton
        20: dup
        21: invokespecial #4                  // Method "<init>":()V
        24: putstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
        27: aload_0
        28: monitorexit
        29: goto          37
        32: astore_1
        33: aload_0
        34: monitorexit
        35: aload_1
        36: athrow
        37: getstatic     #2                  // Field instance:Lcom/java/sjq/base/markdown/Singleton;
        40: areturn
      Exception table:
         from    to  target type
            11    29    32   any
            32    35    32   any
      LineNumberTable:
        line 8: 0
        line 9: 6
        line 10: 11
        line 11: 17
        line 13: 27
        line 15: 37
      StackMapTable: number_of_entries = 3
        frame_type = 252 /* append */
          offset_delta = 27
          locals = [ class java/lang/Object ]
        frame_type = 68 /* same_locals_1_stack_item */
          stack = [ class java/lang/Throwable ]
        frame_type = 250 /* chop */
          offset_delta = 4
}
SourceFile: "Singleton.java"

Process finished with exit code 0

```

## 重排序原则
as-if-serial 原则、happen-before 原则。

## Java指令会重排序规则：
Java指令会重排序是因为Java编译器和处理器为了提高程序的执行效率，可能会对指令进行重排序。但是，Java编译器和处理器在进行指令重排序时，必须遵守一定的规则，即保证程序的执行结果与源代码中的顺序一致。这些规则包括：

1. 程序顺序规则：即在一个线程内，按照程序代码的顺序，前面的操作先行发生于后面的操作。

2. 管程锁定规则：即在一个线程对一个对象进行加锁的操作先行发生于后面对该对象进行解锁的操作。

3. volatile变量规则：即对一个volatile变量的写操作先行发生于后面对该变量的读操作。

4. 传递性：即如果操作A先行发生于操作B，操作B先行发生于操作C，那么操作A先行发生于操作C。

5. 线程启动规则：即线程的start()方法先行发生于该线程的每个操作。

6. 线程终止规则：即线程的所有操作都先行发生于该线程的终止检测，可以通过Thread.join()方法实现。

7. 中断规则：即一个线程中断另一个线程，中断操作先行发生于被中断线程的代码检测到中断事件的操作。

8. 对象终结规则：即一个对象的初始化完成先行发生于它的finalize()方法的开始。


因此，Java指令会重排序是为了提高程序的执行效率，但是必须遵守一定的规则，保证程序的执行结果与源代码中的顺序一致。