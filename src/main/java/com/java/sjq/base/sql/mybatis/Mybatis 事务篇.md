[深入浅出Mybatis系列（五）Mybatis事务篇](https://blog.csdn.net/Message_lx/article/details/89533045)

> 在学习Mysql事务开始,分为两步。一.先看下Mysql的事务级别都有什么,然后看Mysql的事务级别设置命令。及常见问题。二.JDK是如何处理数据库操作的呢? [Mybatis](https://so.csdn.net/so/search?q=Mybatis&spm=1001.2101.3001.7020)是如何实现JDK定义的事务级别操作。

### 一.Mysql的事务级别及常见概念

-   MySQL事务隔离级别

| 事务隔离级别                 | 脏读 | 不可重复读 | 幻读 | 解释                                                   |
| ---------------------------- | ---- | ---------- | ---- | ------------------------------------------------------ |
| 读未提交（read-uncommitted） | 是   | 是         | 是   | 可以读到未提交的事物                                   |
| 不可重复读（read-committed） | 否   | 是         | 是   | 只能读提交的事物                                       |
| 可重复读（repeatable-read）  | 否   | 否         | 是   | 事务提交前后都能读【MySql默认】                        |
| 串行化（serializable）       | 否   | 否         | 否   | serializable时会锁表,是最安全的,也是日常开发基本不会用 |

-   Mysql事务设置命令
    
-   读未提交  
    `set session transaction isolation level read uncommitted;`
    
-   不可重复读(读已提交的)  
    `set session transaction isolation level read committed;`
    
-   可重复读  
    `set session transaction isolation level repeatable read;`
    
-   串行化  
    `set session transaction isolation level serializable;`
    

举一个例子，我做了一个事务未提交,默认情况是看不到的，当设置了读未提交sql,就可以看到了  
![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/7fb609520ffe48987205f796999873bb.png)

以上这四个事务级别最不常用的是串行化,因为他是最严格的会加行锁或者表锁。当一个事务在执行查询操作，其他的连接就不允许对表进行写操作,只允许读。显然这是不满足开发的，想想一下，支付宝当一个人A在查询账单时候，其他人B就不能消费了。因为A在对账单表进行查询，B要对账单表进行修改。

最适合的是可重复读，因为只会加行级锁。当两个事务同时进行时，其中一个事务修改数据对另一个事务不会造成影响，即使修改的事务已经提交也不会对另一个事务造成影响。  
(当一个事务执行中他看到的数据是不会改变的，即在一个事务中不论查询数据多少次都不会改变，即便其他人对这个数据进行了修改，也看不到，只有在当前事务提交之后，然后在查询才能看到其他人的改变，这就解决了不可重复读和脏读的问题，但是会造成幻读)

-   √: 可能出现
-   ×: 不会出现

|脏读| 不可重复读 |幻读|  
—|—|—|—  
|Read uncommitted| √| √| √|  
|Read committed| ×| √| √|  
|Repeatable read| ×| × |√|  
|Serializable| ×| ×| ×|

-   什么是不可重复读?

就是说在一个事务中，查询了多次数据，每次看到的都不一样。因为别人对数据进行了修改。

-   事务的并发问题

1、脏读：事务A读取了事务B更新的数据，然后B回滚操作，那么A读取到的数据是脏数据

2、不可重复读：事务 A 多次读取同一数据，事务 B 在事务A多次读取的过程中，对数据作了更新并提交，导致事务A多次读取同一数据时，结果 不一致。

3、幻读：系统管理员A将数据库中所有学生的成绩从具体分数改为ABCDE等级，但是系统管理员B就在这个时候插入了一条具体分数的记录，当系统管理员A改结束后发现还有一条记录没有改过来，就好像发生了幻觉一样，这就叫幻读。

小结：不可重复读的和幻读很容易混淆，不可重复读侧重于修改，幻读侧重于新增或删除。解决不可重复读的问题只需锁住满足条件的行(Repeatable read)，解决幻读需要锁表

-   事务的基本要素（ACID）

1、原子性（Atomicity）：事务开始后所有操作，要么全部做完，要么全部不做，不可能停滞在中间环节。事务执行过程中出错，会回滚到事务开始前的状态，所有的操作就像没有发生一样。也就是说事务是一个不可分割的整体，就像化学中学过的原子，是物质构成的基本单位。

2、一致性（Consistency）：事务开始前和结束后，数据库的完整性约束没有被破坏 。比如A向B转账，不可能A扣了钱，B却没收到。

3、隔离性（Isolation）：同一时间，只允许一个事务请求同一数据，不同的事务之间彼此没有任何干扰。比如A正在从一张银行卡中取钱，在A取钱的过程结束前，B不能向这张卡转账。

4、持久性（Durability）：事务完成后，事务对数据库的所有更新将被保存到数据库，不能回滚。

[参考地址](https://www.cnblogs.com/huanongying/p/7021555.html)

### 二.JDK是如何处理数据库操作的呢? Mybatis是如何实现JDK定义的事务级别操作。

JDK是如何处理数据库操作的呢? 其实更直接操作MySql数据库命令一样,只不过Java进行了命令的封装,是你在调用Java方法时候就自动执行了命令,下面我们看看吧.

1.  定义底层通用接口,实现交给框架开发者来实现。

Connection。事务级JDK已经定义好了

```
public interface Connection  extends Wrapper, AutoCloseable {
    /**
     * 表示不支持事务的常量。
     */
    int TRANSACTION_NONE             = 0;
   /**
     * 可读到未提交
     */
    int TRANSACTION_READ_UNCOMMITTED = 1;
    /**
     * 只能读已提交
     */
    int TRANSACTION_READ_COMMITTED   = 2;
   /**
     * 可重复读
     */
    int TRANSACTION_REPEATABLE_READ  = 4;
   /**
     * 串行化操作
     */
    int TRANSACTION_SERIALIZABLE     = 8;
   /**
     * 设置事务
     */
    void setTransactionIsolation(int level) throws SQLException;
}

```

留给开发者自己实现,下图Mybatis中`ConnectionImpl`实现。在进行数据库操作前,会先执行设置事务。  
![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/915d3a9c3c61742f636c8a5e162f6268.png)

### JdbcTransaction如何设置事务呢?

在打开数据库连接时候设置事务  
![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/c0ccb9b94abef0335ec3b99f37f5f6db.png)

根据事务级别,执行不同的sql命令。  
![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/915d3a9c3c61742f636c8a5e162f6268.png)

以上就是事务的底层实现,那么我们在带到项目中来看看,以一个数据库操作的例子来看看Mybatis的调用过程吧。

```
@Test
  public void transactionTest(){
    //拿到mybatis的配置文件输入流
    InputStream mapperInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("mybatisConfig.xml");
    //SqlSessionFactoryBuilder通过XMLConfigBuilder解析器读取配置信息生成Configuration信息
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mapperInputStream);
    SqlSession sqlSession = sqlSessionFactory.openSession(TransactionIsolationLevel.READ_COMMITTED);
    TUserMapper mapper = sqlSession.getMapper(TUserMapper.class);
    TUser tUser = new TUser();
    tUser.setId(10233);
    tUser.setName("事务测试");
    mapper.insert(tUser);
//    sqlSession.commit();
    sqlSession.rollback(true);
  }

```

SqlSession就是数据库的会话,要想创建数据库会话,首先要打开数据库连接。由此来判断事务级别就是在SqlSessionFactory,在获取SqlSession的时候来执行sql语句的。我们看流程图

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/239e207b72e0160f1ecc6fb303c552e1.png)

此时事务还并没有设置，只有在  
`SimpleExecutor`执行任意数据库操作时候才会调用

```
@Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
      //执行事务
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }
 private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    //JdbcTransaction打开连接。
    Connection connection = getConnection(statementLog);
    stmt = handler.prepare(connection, transaction.getTimeout());
    handler.parameterize(stmt);
    return stmt;
  }
```

最终调用  
JdbcTransaction打开连接。

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E7%AF%87.assets/c0ccb9b94abef0335ec3b99f37f5f6db.png)

以上就是Mybatis中的事务级别控制。但是这里所说的除了在学习源码时候会看到，在日常开发中不会经常看到。大部分Java的开发都是基于Spring框架之上,Spring对事务的处理有进一步的处理。Spring提出了事务传播方式的概念，这个概念怎么理解呢?

### Spring的事务传播方式

前面所说的事务,是不涉及嵌套条调用的,即是不会再事务中嵌套事务调用。但是在开发中难免会遇到这种情况。  
比如，一个事务在没有执行[commit](https://so.csdn.net/so/search?q=commit&spm=1001.2101.3001.7020)之前，有调用了一个事务。  
那这个时候，遇到两个都有事务的方法怎么办呢，因为Spring提出了事务传播方式这个概念。

1、PROPAGATION\_REQUIRED：如果当前没有事务，就创建一个新事务，如果当前存在事务，就加入该事务，该设置是最常用的设置。

2、PROPAGATION\_SUPPORTS：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务执行。‘

3、PROPAGATION\_MANDATORY：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常。

4、PROPAGATION\_REQUIRES\_NEW：创建新事务，无论当前存不存在事务，都创建新事务。

5、PROPAGATION\_NOT\_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。

6、PROPAGATION\_NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。

7、PROPAGATION\_NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION\_REQUIRED类似的操作。

**eg:**

当前是有事务的,如果调用方法中也有一个含有事务的接口,那么就放弃里面的事务，而加入到当前的事务

```
   @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
   public String doOpt() throws Exception {
    ...
   }
```

### Spring的事务传播方式的实现类

AbstractPlatformTransactionManager使用的技术手段就是代理。

其实就是set autocommit=off; 当方法执行完成无指定的异常,在进行commit;

下面我们自己来写伪代码

**MyTransactional**

```
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyTransactional {
  //传播方式
  Propagation propagation() default Propagation.REQUIRED;
  //捕捉异常
  Class<? extends Throwable>[] rollbackFor() default {};
}
```

**SqlSession**保证每个线程一个连接实例

```

public class SqlSession {

  private static ThreadLocal<Connection> connections = new ThreadLocal();

  static {
    try {//注册驱动，反射方式加载
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
    }
  }

  public SqlSession(boolean autocommit, int level) {
    try {
      String url = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
      //设置用户名
      String username = "root";
      //设置密码
      String password = "";
      //获得连接对象
      Connection con = DriverManager.getConnection(url, username, password);
      con.setAutoCommit(autocommit);
      setTransactionIsolation(level, con.createStatement());
      connections.set(con);
    } catch (Exception e) {
    }
  }


  public void execute(String sql) {
    try {
      Console.normal("执行Sql: " + sql);
//      connections.get().createStatement().execute(sql)
    } catch (Exception e) {
    }
  }

  private static void setTransactionIsolation(int level, Statement statement) throws SQLException {
    String sql;
    switch (level) {
      case 0:
        throw SQLError.createSQLException(Messages.getString("Connection.24"), null);
      case 1:
        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
        break;
      case 2:
        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
        break;
      case 3:
      case 5:
      case 6:
      case 7:
      default:
        throw SQLError.createSQLException(Messages.getString("Connection.25", new Object[]{level}), "S1C00", null);
      case 4:
        sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
        break;
      case 8:
        sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
    }
    Console.customerNormal("设置事务级别Sql: ", sql);
//    statement.execute(sql);
  }

  public void commit() throws Exception {
//    Connection connection = connections.get();
//    connection.commit();
    Console.customerNormal("执行提交Sql:", "commit;");
  }

  public void rollback() throws Exception {
//    Connection connection = connections.get();
//    connection.rollback();
    Console.customerNormal("设置事务级别Sql: ", "roolback");
  }

}

```

**UserService**

```
public interface UserService {
  void save();
}
public class UserServiceImpl implements UserService {
  SqlSession sqlSession = new SqlSession(false, 4);
  @MyTransactional(propagation = Propagation.REQUIRED, rollbackFor = ArithmeticException.class)
  public void save() {
    //执行sql1
    sqlSession.execute("select * from t");
    try {
      int i = 2 / 0;
    } catch (Exception e) {
      throw e;
    }
    //执行sql2
    sqlSession.execute("select * from t");
  }

```

**测试代码**

```
 public static void main(String[] args) {
    
    UserService userService = new UserServiceImpl();
    //生成JDK代理
    UserService userServiceProxy = (UserService) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{UserService.class}, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method declaredMethod = userService.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        MyTransactional declaredAnnotation = declaredMethod.getDeclaredAnnotation(MyTransactional.class);
        Class<? extends Throwable>[] classes = declaredAnnotation.rollbackFor();
        //传播方式处理逻辑
        //.....
        Object res = null;
        try {
          res = declaredMethod.invoke(userService);
        } catch (Exception e) {
          Throwable cause = e.getCause();
          if (cause.getClass().equals(classes[0])) {
            //执行回滚
            ((UserServiceImpl) userService).sqlSession.rollback();
            return res;
          }
        }
        //执行提交
        ((UserServiceImpl) userService).sqlSession.commit();
        return res;
      }
    });
    userServiceProxy.save();
  }
```

#### 结果

-   成功

```
[设置事务级别Sql: ]: SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ
[正常]: 执行Sql: update T_USER set name = 'china' where id = 1;
[正常]: 执行Sql: update T_USER set name = '中国' where id = 1;
[执行提交Sql:]: commit;
```

-   失败

```
[设置事务级别Sql: ]: SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ
[正常]: 执行Sql: update T_USER set name = 'china' where id = 1;
[设置事务级别Sql: ]: roolback
```

以上就是Mybatis关于事务小编想说的。有问题可以提出，我们一起学习，如果有写错的地方或者想讨论的，希望能提出，再次感谢!