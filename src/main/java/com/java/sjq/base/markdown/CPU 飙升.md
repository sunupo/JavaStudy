# 面试官：应用上线后Cpu使用率飙升如何排查？

上次面试官问了个问题：**应用上线后Cpu使用率飙升如何排查？**

其实这是个很常见的问题，也非常简单，那既然如此我为什么还要写呢？因为上次回答的时候我忘记将线程PID转换成16进制的命令了。

所以我决定再重温一遍这个问题，当然贴心的我还给大家准备好了测试代码，大家可以实际操作一下，这样下次就不会忘记了。

## 模拟一个高CPU场景

```
public class HighCpuTest {
    public static void main(String[] args) {
        List<HignCpu> cpus = new ArrayList<>();

        Thread highCpuThread = new Thread(()->{
            int i = 0;
            while (true){
                HignCpu cpu = new HignCpu("Java日知录",i);

                cpus.add(cpu);
                System.out.println("high cpu size:" + cpus.size());
                i ++;
            }
        });
        highCpuThread.setName("HignCpu");
        highCpuThread.start();
    }
}
复制代码

```

在main方法中开启了一个线程，无限构建`HighCpu`对象。

```
@Data
@AllArgsConstructor
public class HignCpu {
    private String name;
    private int age;
}
复制代码

```

准备好上面的代码，运行HighCpuTest，然后就可以开始一些列的操作来发现问题原因了。

## 排查步骤

### 第一步，使用 top 找到占用 CPU 最高的 Java 进程

```
1. 监控cpu运行状，显示进程运行信息列表
top -c

2. 按CPU使用率排序，键入大写的P
P
复制代码

```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7d533c597bac439f84541251ac0b0d4c~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)

### 第二步，用 `top -Hp` 命令查看占用 CPU 最高的线程

上一步用 `top`命令找到了那个 Java 进程。那一个进程中有那么多线程，不可能所有线程都一直占着 CPU 不放，这一步要做的就是揪出这个罪魁祸首，当然有可能不止一个。

执行`top -Hp pid`命令，pid 就是前面的 Java 进程，我这个例子中就是 `16738` ，完整命令为：

`top -Hp 16738`，然后键入P (大写p)，线程按照CPU使用率排序

执行之后的效果如下

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a381dfe9e7ab4d0593505288811436c3~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)

查到占用CPU最高的那个线程 PID 为 16756

### 第三步，查看堆栈信息，定位对应代码

通过printf命令将其转化成16进制，之所以需要转化为16进制，是因为堆栈里，线程id是用16进制表示的。（我当时就是忘记这个命令了～）

```
[root@review-dev ~]# printf "%x\n" 16756
4174
复制代码

```

得到16进制的线程ID为4174。

通过jstack命令查看堆栈信息

```
jstack 16738 | grep '0x4174' -C10 --color
复制代码

```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5e8507279e044c4f96678d664094088a~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)

如上图，找到了耗CPU高的线程对应的线程名称“HighCpu”，以及看到了该线程正在执行代码的堆栈。

最后，根据堆栈里的信息，定位到对应死循环代码，搞定。

## 小结

**cpu使用率飙升后如何排查**这个问题不仅面试中经常会问，而且在实际工作中也非常有用，大家最好根据上述步骤实际操作一下，这样才能记得住记得牢。