# 一、[如何优雅地关闭一个线程](https://mp.weixin.qq.com/s/Yfr0sIWjBYd7oMU6GzRZtg)

如何关闭比线程 a

1. 在其他线程调用 `a.interrupt`。
2. a 线程中用 `InterruptedException`，`Thread.interrupted()` 或者 `Thread.currentThread().isInterrupted()` 来处理。

# 二、[线程池 — 停止线程池中线程、正确处理线程池中线程的异常](https://blog.csdn.net/weixin_48655626/article/details/109250330)


## 2.1 提交给线程池的线程要是可以被中断的

ExecutorService线程池提供了两个很方便的停止线程池中线程的方法，他们是shutdown和shutdownNow。

- shutdown不会接受新的任务，但是会等待现有任务执行完毕。
- 而shutdownNow会尝试立马终止现有运行的线程。

那么它是怎么实现的呢？我们看一个ThreadPoolExecutor中的一个实现：

```
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

```

里面有一个interruptWorkers()方法的调用，实际上就是去中断当前运行的线程。

所以我们可以得到一个结论，提交到ExecutorService中的任务一定要是可以被中断的，否则shutdownNow方法将会失效。

```
    public void correctSubmit(){
        Runnable runnable= ()->{
            try(SocketChannel  sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080))) {
                ByteBuffer buf = ByteBuffer.allocate(1024);
                while(!Thread.interrupted()){
                    sc.read(buf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ExecutorService pool =  Executors.newFixedThreadPool(10);
        pool.submit(runnable);
        pool.shutdownNow();
    }

```

我们需要在while循环中加上中断的判断，从而控制程序的执行。

## 2.2 正确处理线程池中线程的异常

如果在线程池中的线程发生了异常，比如RuntimeException，我们怎么才能够捕捉到呢？ 如果不能够对异常进行合理的处理，那么将会产生不可预料的问题。

看下面的例子：

```
    public void wrongSubmit() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        Runnable runnable= ()->{
            throw new NullPointerException();
        };
        pool.execute(runnable);
        Thread.sleep(5000);
        System.out.println("finished!");
    }

```

上面的例子中，我们submit了一个任务，在任务中会抛出一个NullPointerException，因为是非checked异常，所以不需要显式捕获，在任务运行完毕之后，我们基本上是不能够得知任务是否运行成功了。

那么，怎么才能够捕获这样的线程池异常呢？这里介绍大家几个方法。

### 2.2.1 第一种方法就是继承ThreadPoolExecutor，重写

```
 protected void afterExecute(Runnable r, Throwable t) { }

```

和

```
protected void terminated() { }

```

这两个方法。

其中afterExecute会在任务执行完毕之后被调用，Throwable t中保存的是可能出现的运行时异常和Error。我们可以根据需要进行处理。

而terminated是在线程池中所有的任务都被调用完毕之后才被调用的。我们可以在其中做一些资源的清理工作。

### 2.2.2 第二种方法就是使用 UncaughtExceptionHandler。
```java
Thread.setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler);
```

Thread类中提供了一个setUncaughtExceptionHandler方法，用来处理捕获的异常，我们可以在创建Thread的时候，为其添加一个UncaughtExceptionHandler就可以了。

但是ExecutorService执行的是一个个的Runnable，怎么使用ExecutorService来提交Thread呢？

别怕， Executors在构建线程池的时候，还可以让我们传入ThreadFactory，从而构建自定义的Thread。

```
    public void useExceptionHandler() throws InterruptedException {
        ThreadFactory factory =
                new ExceptionThreadFactory(new MyExceptionHandler());
        ExecutorService pool =
                Executors.newFixedThreadPool(10, factory);
        Runnable runnable= ()->{
            throw new NullPointerException();
        };
        pool.execute(runnable);
        Thread.sleep(5000);
        System.out.println("finished!");
    }

    public static class ExceptionThreadFactory implements ThreadFactory {
        private static final ThreadFactory defaultFactory =
                Executors.defaultThreadFactory();
        private final Thread.UncaughtExceptionHandler handler;

        public ExceptionThreadFactory(
                Thread.UncaughtExceptionHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public Thread newThread(Runnable run) {
            Thread thread = defaultFactory.newThread(run);
            thread.setUncaughtExceptionHandler(handler);
            return thread;
        }
    }

    public static class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {

        }
    }

```
### 2.2.3 通过 submit 提交任务返回的 Future 对象，调用 future.get()
上面的例子有点复杂了， 有没有更简单点的做法呢？

有的。ExecutorService除了execute来提交任务之外，还可以使用submit来提交任务。不同之处是submit会返回一个Future来保存执行的结果。

```
    public void useFuture() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        Runnable runnable= ()->{
            throw new NullPointerException();
        };
        Future future = pool.submit(runnable);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Thread.sleep(5000);
        System.out.println("finished!");
    }

```

当我们在调用future.get()来获取结果的时候，异常也会被封装到ExecutionException，我们可以直接获取到。

## 3 线程池中使用ThreadLocal一定要注意清理

我们知道ThreadLocal是Thread中的本地变量，如果我们在线程的运行过程中用到了ThreadLocal，那么当线程被回收之后再次执行其他的任务的时候就会读取到之前被设置的变量，从而产生未知的问题。

正确的使用方法就是在线程每次执行完任务之后，都去调用一下ThreadLocal的remove操作。

或者在自定义ThreadPoolExecutor中，重写beforeExecute(Thread t, Runnable r)方法，在其中加入ThreadLocal的remove操作。
