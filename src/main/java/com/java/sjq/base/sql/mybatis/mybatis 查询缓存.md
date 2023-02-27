[为什么？Mybatis的一级和二级缓存都不建议使用？](https://blog.csdn.net/Park33/article/details/126399272?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-126399272-blog-122029127.pc_relevant_multi_platform_whitelistv4&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-126399272-blog-122029127.pc_relevant_multi_platform_whitelistv4&utm_relevant_index=1)

## 一级缓存引起脏读

一级缓存是BaseExecutor中的一个成员变量localCache（对HashMap的一个简单封装），因此一级缓存的生命周期与SqlSession相同。

```java
//BaseExecutor
protected PerpetualCache localCache;

```



「MyBatis的一级缓存最大范围是SqlSession内部，有多个SqlSession或者分布式的环境下，数据库写操作会引起脏数据，建议设定缓存级别为Statement，即进行如下配置：

```xml
<setting name="localCacheScope" value="STATEMENT"/>
```


原因也很简单，看BaseExecutor的query()方法，当配置成STATEMENT时，每次查询完都会清空缓存。

![img](https://img-blog.csdnimg.cn/img_convert/e9fcc041ccf8e28564922f259b729f1c.jpeg)

「看到这你可能会想，我用mybatis后没设置这个参数啊，好像也没发生脏读的问题啊，其实是因为你和spring整合了」

当mybatis和spring整合后（整合的相关知识后面还有一节）
在未开启事务的情况之下，每次查询，spring都会关闭旧的sqlSession而创建新的sqlSession，因此此时的一级缓存是没有起作用的

在开启事务的情况之下，spring使用threadLocal获取当前线程绑定的同一个sqlSession，因此此时一级缓存是有效的，当事务执行完毕，会关闭sqlSession

## 二级缓存

二级缓存是Configuration对象的成员变量，因此二级缓存的生命周期是整个应用级别的。并且是基于namespace构建的，一个namesapce构建一个缓存。

```java
// Configuration
protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");

```

「二级缓存不像一级缓存那样查询完直接放入一级缓存，而是要等事务提交时才会将查询出来的数据放到二级缓存中。」

因为如果事务1查出来直接放到二级缓存，此时事务2从二级缓存中拿到了事务1缓存的数据，但是事务1回滚了，此时事务2不就发生了脏读了吗？

### **「二级缓存的相关配置有如下3个」**

#### 「1.mybatis-config.xml」

```xml
<settings>
    <setting name="cacheEnabled" value="true"/>
</settings>

```

这个是二级缓存的总开关，只有当该配置项设置为true时，后面两项的配置才会有效果

#### 「2.mapper映射文件中」

mapper映射文件中如果配置了<cache>和<cache-ref>中的任意一个标签，则表示开启了二级缓存功能，没有的话表示不开启

```xml
<cache type="" eviction="FIFO" size="512"></cache>
```

二级缓存的部分配置如上，type就是填写一个全类名，用来指定二级缓存的实现类，这个实现类需要实现Cache接口，默认是PerpetualCache（你可以利用这个属性将mybatis二级缓存和Redis，Memcached等缓存组件整合在一起）

#### 「3.节点中的useCache属性」

该属 性表示查询产生的结果是否要保存的二级缓存中，useCache属性的默认值为true，这个配置可以将二级缓存细分到语句级别

