| PROPAGATION_XXX | 没有事务       | 存在事务       |
| --------------- | -------------- | -------------- |
| REQUIRED        | 创建new        | 加入++         |
| SUPPORT         | 加入++         | 【非事务执行】 |
| MANDARY         | 异常！！！     | 加入++         |
| NEVER           | 【非事务执行】 | 异常！！！     |
| REQUIRED_NEW    | 创建new        | 创建new        |
| NOT_SUPPORT     | 【非事务执行】 | 挂起suspend    |
| NESTED          | 创建new        | 嵌套执行       |

## PROPAGATION_NESTED 和PROPAGATION_REQUIRES_NEW 区别

      1. 开启事务的多少，PROPAGATION_REQUIRES_NEW会开启一个新事务，外部事务挂起，里面的事务独立执行。PROPAGATION_NESTED为父子事务，实际上是借助jdbc的savepoint实现的，属于同一个事物。 
      2. PROPAGATION_NESTED的回滚可以总结为，子事务回滚到savepoint，父事务可选择性回滚或者不不滚；父事务回滚子事务一定回滚。PROPAGATION_REQUIRES_NEW则是不同事物，嵌套事务之间没有必然联系是否回滚都由自己决定。
  ————————————————
  版权声明：本文为CSDN博主「Big_Blogger」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
  原文链接：https://blog.csdn.net/Big_Blogger/article/details/70184627



| PROPAGATION_REQUIRES_NEW | 内部事务 | 外部事物                                                 |
| ------------------------ | -------- | -------------------------------------------------------- |
| 内部回滚rollback         |          |                                                          |
| 内部提交commit           |          |                                                          |
| 外部回滚rollback         |          |                                                          |
| 外部提交commit           |          |                                                          |
| 内部异常exception        | 回滚     | 回滚<br />（在外部没有try catch 包裹住内部事务的前提下） |
| 外部异常exception        | 正常√    | 回滚                                                     |





| PROPAGATION_NESTED | 内部事务                                                     | 外部事物                                                 |
| ------------------ | ------------------------------------------------------------ | -------------------------------------------------------- |
| 内部回滚rollback   | 内部事务 rollback, 那么首先内部回滚到它执行之前的 Save Point (在任何情况下都会如此)。 | 外部事务自己决定是 commit 还是 rollback                  |
| 内部提交commit     |                                                              |                                                          |
| 外部回滚rollback   |                                                              |                                                          |
| 外部提交commit     | 如果外部事务 [commit](https://so.csdn.net/so/search?q=commit&spm=1001.2101.3001.7020), 潜套事务也会被 commit。 |                                                          |
| 内部异常exception  | 回滚                                                         | 回滚<br />（在外部没有try catch 包裹住内部事务的前提下） |
| 外部异常exception  | 回滚                                                         | 回滚                                                     |

> [(117条消息) Propagation.NESTED 和Propagation.REQUIRES\_NEW的区别\_aiyaya\_66da的博客-CSDN博客\_propagation.nested](https://blog.csdn.net/aiyaya_66da/article/details/94171771)
>
> 

## 测试

> [PROPAGATION_NESTED 和PROPAGATION_REQUIRES_NEW 区别](https://blog.csdn.net/Big_Blogger/article/details/70184627?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-70184627-blog-105391052.pc_relevant_aa2&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-70184627-blog-105391052.pc_relevant_aa2&utm_relevant_index=1)

```java
methodA(){
	methodB();
}
```

### 一、 methodA  PROPAGATION_REQUIRED  + methodB  PROPAGATION_REQUIRES _NEW 

1. 若A回滚，B可以成功执行

2. 若B回滚，A catch异常可自行决定是否回滚，可作为分支处理（不推荐）

### 二、methodA  PROPAGATION_REQUIRED + methodB  PROPAGATION_NESTED 

1. 若A回滚，一定回滚

2. 若B回滚，A catch异常可自行决定是否回滚，可作为分支处理（不推荐）





![聊聊Spring事务失效的12种场景，太坑人了](spring%20%E7%9A%84%E4%BA%8B%E5%8A%A1%E4%BC%A0%E6%92%AD%E8%A1%8C%E4%B8%BA.assets/e33dd29232e40c3763913a3de64ec972.png)

