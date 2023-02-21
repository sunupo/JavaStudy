[(113条消息) 数据库之快照读与当前读\_当前读和快照读\_悠然予夏的博客-CSDN博客](https://blog.csdn.net/weixin_52851967/article/details/125027486)



## 0、普通读

为了提升性能，RR与RC下的普通读都是快照读，这里提到的普通读,  是指除了如下2种之外的select都是普通读

```
select * from table where ... for update;select * from table where ... lock in share mode;
```

## **1、当前读**

即读取最新提交的数据

-   select … for update
    
-   select ... lock in share mode
    
-   insert、update、delete，都会按最新提交的数据进行操作
    

当前读本质上是基于锁的并发读操作

## **2、快照读**（一致性读）

读取某一个快照建立时（可以理解为某一时间点）的数据，也称为一致性读。快照读主要体现在 select 时，而不同[隔离级别](https://so.csdn.net/so/search?q=%E9%9A%94%E7%A6%BB%E7%BA%A7%E5%88%AB&spm=1001.2101.3001.7020)下，select 的行为不同

-   在 Serializable 隔离级别下 - 普通 select 也变成当前读，即加共享读锁
    
-   在 RC 隔离级别下 - 每次 select 都会建立新的快照
    
-   在 RR 隔离级别下
    
    -   事务启动后，首次 select 会建立快照
        
    -   如果事务启动选择了 with consistent [snapshot](https://so.csdn.net/so/search?q=snapshot&spm=1001.2101.3001.7020)，事务启动时就建立快照
        
    -   基于旧数据的修改操作，会重新建立快照
        

快照读本质上读取的是历史数据（原理是回滚段），属于无锁查询。

> 在MySQL 中，只有RR与RC才会使用快照读。RR与RC下的普通读都是快照读，但两者的快照读有所不同：
>
> -   RC下，事务内每次都是读最新版本的快照数据
>
> -   RR下，事务内每次都是读同一版本的快照数据(即首次read时的版本)

### **2.1、RR 下，快照建立时机 - 第一次 select 时**

| **tx1**                                                      | **tx2**                                              |
| ------------------------------------------------------------ | ---------------------------------------------------- |
| set session transaction isolation level repeatable read;     |                                                      |
| start transaction;                                           |                                                      |
| select \* from account; /\* 此时建立快照，两个账户为 1000 \*/ |                                                      |
|                                                              | update account set balance = 2000 where accountNo=1; |
| select \* from account; /\* 两个账户仍为 1000 \*/            |                                                      |

-   快照一旦建立，以后的查询都基于此快照，因此 tx1 中第二次 select 仍然得到 1 号账户余额为 1000
    

**如果 tx2 的 update 先执行**

| **tx1**                                                      | **tx2**                                              |
| ------------------------------------------------------------ | ---------------------------------------------------- |
| set session transaction isolation level repeatable read;     |                                                      |
| start transaction;                                           |                                                      |
|                                                              | update account set balance = 2000 where accountNo=1; |
| select \* from account; /\* 此时建立快照，1号余额已经为2000 \*/ |                                                      |

### **2.2、RR 下，快照建立时机 - 事务启动时**

如果希望事务启动时就建立快照，可以添加 **with consistent snapshot** 选项

| **tx1**                                                      | **tx2**                                              |
| ------------------------------------------------------------ | ---------------------------------------------------- |
| set session transaction isolation level repeatable read;     |                                                      |
| start transaction with consistent snapshot; /\* 此时建立快照，两个账户为 1000 \*/ |                                                      |
|                                                              | update account set balance = 2000 where accountNo=1; |
| select \* from account; /\* 两个账户仍为 1000 \*/            |                                                      |

### **2.3、RR 下，快照建立时机 - 修改数据时**  

| **tx1**                                                      | **tx2**                                                    |
| ------------------------------------------------------------ | ---------------------------------------------------------- |
| set session transaction isolation level repeatable read;     |                                                            |
| start transaction;                                           |                                                            |
| select \* from account; /\* 此时建立快照，两个账户为 1000 \*/ |                                                            |
|                                                              | update account set balance=balance+1000 where accountNo=1; |
| update account set balance=balance+1000 where accountNo=1;   |                                                            |
| select \* from account; /\* 1号余额为3000 \*/                |                                                            |

-   tx1 内的修改必须重新建立快照，否则，就会发生丢失更新的问题