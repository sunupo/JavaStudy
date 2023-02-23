[(116条消息) 【学习】线程池：线程池提交任务的方式execute与submit的区别\_沐子·李的博客-CSDN博客](https://blog.csdn.net/u010328311/article/details/122848897)

### [线程池](https://so.csdn.net/so/search?q=%E7%BA%BF%E7%A8%8B%E6%B1%A0&spm=1001.2101.3001.7020)：系列二

-   -   [这三种线程池的方式有风险](https://blog.csdn.net/u010328311/article/details/122848897#_2)
-   -   [FixedThreadPool（固定大小的线程池）](https://blog.csdn.net/u010328311/article/details/122848897#FixedThreadPool_4)
-   [SingleThreadExecutor（单个线程的线程池）](https://blog.csdn.net/u010328311/article/details/122848897#SingleThreadExecutor_10)
-   [CachedThreadPool（可缓存的线程池）](https://blog.csdn.net/u010328311/article/details/122848897#CachedThreadPool_15)
-   [execute与submit的区别](https://blog.csdn.net/u010328311/article/details/122848897#executesubmit_19)
-   -   [execute代码案例](https://blog.csdn.net/u010328311/article/details/122848897#execute_20)
-   [submit所包含三种方法](https://blog.csdn.net/u010328311/article/details/122848897#submit_72)
-   -   [Future所包含方法](https://blog.csdn.net/u010328311/article/details/122848897#Future_74)
-   [submit(Runable，Future<?>)代码案例](https://blog.csdn.net/u010328311/article/details/122848897#submitRunableFuture_77)
-   [submit(Runable，Future)代码案例](https://blog.csdn.net/u010328311/article/details/122848897#submitRunableFutureT_126)
-   [submit(Callable，Future)代码案例](https://blog.csdn.net/u010328311/article/details/122848897#submitCallableFutureT_176)
-   -   [首先创建任务类实现Callable](https://blog.csdn.net/u010328311/article/details/122848897#Callable_177)
-   [线程池执行任务](https://blog.csdn.net/u010328311/article/details/122848897#_195)
-   [总结二者区别](https://blog.csdn.net/u010328311/article/details/122848897#_246)
-   [参考地址](https://blog.csdn.net/u010328311/article/details/122848897#_249)

## 这三种线程池的方式有风险

![在这里插入图片描述](https://img-blog.csdnimg.cn/b575fd8d1fcb45698091eb6d96cc9648.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

### FixedThreadPool（固定大小的线程池）

-   核心线程和最大线程一样，这样空闲线程就不会被销毁
-   允许的请求队列长度为Integer.MAX\_VALUE，可能会堆积大量的请求，从而导致OOM。

![在这里插入图片描述](https://img-blog.csdnimg.cn/dff47ffb10904db88095371a64476409.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

### SingleThreadExecutor（单个线程的线程池）

-   核心线程数和最大线程数都为1，说明全是核心线程
-   允许的请求队列长度为Integer.MAX\_VALUE，可能会堆积大量的请求，从而导致OOM。  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/a4d48b8ef7284d7e83d3095cdc494259.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

### CachedThreadPool（可缓存的线程池）

-   核心线程数为0，最大线程数为Integer.MAX\_VALUE，说明全是空闲线程，空闲线程存活时间为60秒。
-   允许的创建线程数量为Integer.MAX\_VALUE，可能会堆积大量的请求，从而导致OOM。  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/19fb8ad523544de3a8c328bcbfc7896b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

## execute与[submit](https://so.csdn.net/so/search?q=submit&spm=1001.2101.3001.7020)的区别

### execute代码案例

-   首先创建任务类

```
/**
 * @Description 任务
 * @ClassName
 * @Author 
 * @Date 2022/2/6 20:56
 **/
public class Task implements Runnable{

    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}

```

-   单个线程池执行任务

```
import com.naidou.threadPool.tp02.Task;

import java.util.concurrent.*;

/**
 * @Description  execute与submit的区别
 * @ClassName ThreadPool03
 * @Date 2022/2/9 21:12
 **/
public class ThreadPool03 {


    public static void main(String[] args) {
        // 创建任务
        Runnable task = new Task();
        // 创建单个线程池
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        // 提交任务
        testExecute(task,threadPool);
    }
    private static void testExecute(Runnable task,ExecutorService threadPool){
        // 提交任务
        threadPool.execute(task);
        // 关闭线程池
        threadPool.shutdown();
    }
 }
```

-   输出结果

![在这里插入图片描述](https://img-blog.csdnimg.cn/ff7ab7a0cd2743d78a580b17bbf4b958.png)

### submit所包含三种方法

![在这里插入图片描述](https://img-blog.csdnimg.cn/e22088160a7746948ebce84f6465ae34.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

#### Future所包含方法

![在这里插入图片描述](https://img-blog.csdnimg.cn/70cc19c4571b49fa9aea94cd066c5f2b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

#### submit(Runable，Future<?>)代码案例

```
import com.naidou.threadPool.tp02.Task;

import java.util.concurrent.*;

/**
 * @Description  execute与submit的区别
 * @ClassName ThreadPool03
 * @Date 2022/2/9 21:12
 **/
public class ThreadPool03 {


    public static void main(String[] args) {
        // 创建任务
        Runnable task = new Task();
        // 创建单个线程池
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        // 提交任务
        testSubmit(task,threadPool);
    }
    /**
     * @Description submit不确定的类型
     * @param task
     * @param threadPool
     * @Date 2022/2/9 20:37
     * @Return
     **/
    private static void testSubmit(Runnable task,ExecutorService threadPool){
        Future<?> future = threadPool.submit(task);
        try {
            Object result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }
 }
```

-   输出结果  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/f3abcad161b24bd88e702e862e13025b.png)

#### submit(Runable，Future)代码案例

```
import com.naidou.threadPool.tp02.Task;

import java.util.concurrent.*;

/**
 * @Description  execute与submit的区别
 * @ClassName ThreadPool03
 * @Date 2022/2/9 21:12
 **/
public class ThreadPool03 {


    public static void main(String[] args) {
        // 创建任务
        Runnable task = new Task();
        // 创建单个线程池
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        // 提交任务
        testSubmitT(task,threadPool);
    }

    /**
     * @Description submit确定的类型
     * @param task
     * @param threadPool
     * @Date 2022/2/9 20:37
     * @Return
     **/
    private static void testSubmitT(Runnable task,ExecutorService threadPool){
        Future<String> future = threadPool.submit(task,"任务完成");
        try {
            String result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }
 }
```

-   输出结果  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/bb35983c8c6b4f52bb4e6a0b4c2271ce.png)

#### submit(Callable，Future)代码案例

##### 首先创建任务类实现Callable

```
import java.util.concurrent.Callable;

/**
 * @Description Callable任务
 * @ClassName ResultTask
 * @Date 2022/2/9 20:39
 **/
public class ResultTask implements Callable<Integer> {

    public Integer call() throws Exception {
        return 1+1;
    }
}

```

##### 线程池执行任务

```
import com.naidou.threadPool.tp02.Task;

import java.util.concurrent.*;

/**
 * @Description  execute与submit的区别
 * @ClassName ThreadPool03
 * @Date 2022/2/9 21:12
 **/
public class ThreadPool03 {


    public static void main(String[] args) {
        // 创建任务
        ResultTask task1 = new ResultTask();
        // 创建单个线程池
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        // 提交任务
        testSubmitCallable(task1,threadPool);

    }

    /**
     * @Description submit  Callable确定的类型
     * @param task
     * @param threadPool
     * @Date 2022/2/9 20:37
     * @Return
     **/
    private static void testSubmitCallable(ResultTask task,ExecutorService threadPool){
        Future<Integer> future = threadPool.submit(task);
        try {
            Integer result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }
}
```

-   输出结果  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/f427c1a34ab14b4aaedef54f1900306f.png)

### 总结二者区别

![在这里插入图片描述](https://img-blog.csdnimg.cn/b04876e667124903984455ea76de9410.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aW26LGG5a62,size_20,color_FFFFFF,t_70,g_se,x_16)

## 参考地址

[线程池视频讲解03，04](https://www.ixigua.com/6971413229658440205?id=6972132951655973389&logTag=f3aedb86b754302d272f)