[Mybatis事务控制 - 雨也飘柔 - 博客园](https://www.cnblogs.com/xdlrf/p/16824043.html)

## **一、概述**

  对数据库的事务而言，应该具有以下几点：创建（create）、提交（commit）、回滚（rollback）、关闭（close）。对应地，MyBatis将事务抽象成了Transaction接口，其接口定义如下：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/2660057-20221025102716519-1433676168.png)

MyBatis的事务管理分为两种形式：

1.使用JDBC的事务管理机制：即利用java.sql.Connection对象完成对事务的提交（commit()）、回滚（rollback()）、关闭（close()）等。

2.使用MANAGED的事务管理机制：这种机制MyBatis自身不会去实现事务管理，而是让程序的容器如（JBOSS，Weblogic）来实现对事务的管理。

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/2660057-20221025102742531-491570028.png)

这两者的类图如下所示：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/2660057-20221025102806212-75225105.png)

## 二、事务的配置、创建和使用

### **1、事务的配置**

使用MyBatis一般在XML配置文件中定义如下信息：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/ExpandedBlockStart.gif)

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!--映入外部文件定义的属性，供此配置文件使用-->
    <properties resource="jdbc.properties"></properties>
    <environments default="development">
        <!-- 连接环境信息，取一个任意唯一的名字 -->
        <environment id="development">
            <!-- mybatis使用jdbc事务管理方式 -->
            <transactionManager type="jdbc"/>
            <!-- mybatis使用连接池方式来获取连接 -->
            <dataSource type="pooled">
                <!-- 配置与数据库交互的4个必要属性 -->
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>
    <!-- 加载映射文件-->
    <mappers>
        <mapper resource="com/mybatis/EmployeeMapper.xml"/>
    </mappers>   
</configuration>
```

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

<environment>节点定义了连接某个数据库的信息，子节点<transactionManager> 的type 决定用什么类型的事务管理机制。

###  2、**事务工厂的创建**

 MyBatis事务交给了TransactionFactory 事务工厂创建，如果将<transactionManager>的type 配置为"JDBC"。那么，在MyBatis初始化解析<environment>节点时，会根据type="JDBC"创建一个JdbcTransactionFactory工厂，其源码如下：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/ExpandedBlockStart.gif)

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
/**
   * 解析<transactionManager>节点，创建对应的TransactionFactory
   * @param context
   * @return
   * @throws Exception
   */
private TransactionFactory transactionManagerElement(XNode context) throws Exception {
  if (context != null) {
    String type = context.getStringAttribute("type");
    Properties props = context.getChildrenAsProperties();
    /*
          在Configuration初始化的时候，会通过以下语句，给JDBC和MANAGED对应的工厂类
          typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
          typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);
          下述的resolveClass(type).newInstance()会创建对应的工厂实例
     */
    TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
    factory.setProperties(props);
    return factory;
  }
  throw new BuilderException("Environment declaration requires a TransactionFactory.");
}
```

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

如上述代码所示，如果type = "JDBC"，则MyBatis会创建一个JdbcTransactionFactory.class 实例；如果type="MANAGED"，则MyBatis会创建一个MangedTransactionFactory.class实例。MyBatis对<transactionManager>节点的解析会生成 TransactionFactory实例；而对<dataSource>解析会生成datasouce实例。

作为<environment>节点，会根据TransactionFactory和DataSource实例创建一个Environment对象，代码如下所示：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/ExpandedBlockStart.gif)

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
private void environmentsElement(XNode context) throws Exception {
  if (context != null) {
    if (environment == null) {
      environment = context.getStringAttribute("default");
    }
    for (XNode child : context.getChildren()) {
      String id = child.getStringAttribute("id");
      //是和默认的环境相同时，解析之
      if (isSpecifiedEnvironment(id)) {
        //1.解析<transactionManager>节点，决定创建什么类型的TransactionFactory
        TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
        //2. 创建dataSource
        DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
        DataSource dataSource = dsFactory.getDataSource();
        //3. 使用了Environment内置的构造器Builder，传递id 事务工厂TransactionFactory和数据源DataSource
        Environment.Builder environmentBuilder = new Environment.Builder(id)
            .transactionFactory(txFactory)
            .dataSource(dataSource);
        configuration.setEnvironment(environmentBuilder.build());
      }
    }
  }
}
```

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

 Environment表示一个数据库的连接，生成后的Environment对象会被设置到Configuration实例中，以供后续使用。

#### 2.1事务工厂**TransactionFactory**

事务工厂定义了创建**Transaction**的两个方法：

通过指定的Connection对象创建Transaction

通过数据源DataSource来创建Transaction

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/2660057-20221025103531886-1239891301.png)

与JDBC 和MANAGED两种Transaction相对应，TransactionFactory有两个对应的实现的子类：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/2660057-20221025103555022-171924753.png)

### **3、事务Transaction创建**

通过事务工厂TransactionFactory容易获取Transaction对象实例。以JdbcTransaction为例，看下JdbcTransactionFactory是怎样生成JdbcTransaction的，代码如下：

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/ExpandedBlockStart.gif)

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
public class JdbcTransactionFactory implements TransactionFactory {
  public void setProperties(Properties props) {
  }
    /**
     * 根据给定的数据库连接Connection创建Transaction
     */
  public Transaction newTransaction(Connection conn) {
    return new JdbcTransaction(conn);
  }
    /**
     * 根据DataSource、隔离级别和是否自动提交创建Transacion
     */
  public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    return new JdbcTransaction(ds, level, autoCommit);
  }
}
```

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

####  3.1 **JdbcTransaction**

JdbcTransaction直接使用JDBC的提交和回滚事务管理机制。它依赖于从dataSource中取得的连接对象connection 来管理transaction的作用域，connection对象的获取被延迟到调用getConnection()方法。如果autocommit设置为on (开启状态)，它会忽略commit和rollback。即JdbcTransaction使用java.sql.Connection 上的commit和rollback功能，JdbcTransaction相当于对java.sql.Connection事务处理进行了一次包装（wrapper），Transaction的事务管理都是通过java.sql.Connection实现的。

**3.2 ManagedTransaction**

 ManagedTransaction让容器管理事务Transaction的整个生命周期，使用ManagedTransaction的commit和rollback功能不会对事务有任何的影响，它什么都不会做，它将事务管理权利移交给了容器来实现。

![](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/ExpandedBlockStart.gif)

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
/**
 * 让容器管理事务transaction的整个生命周期
 * connection的获取延迟到getConnection()方法的调用
 * 忽略所有的commit和rollback操作
 * 默认情况下，可以关闭一个连接connection，也可以配置它不可以关闭一个连接
 * 让容器来管理transaction的整个生命周期
 */
public class ManagedTransaction implements Transaction {
  private static final Log log = LogFactory.getLog(ManagedTransaction.class);
  
  private DataSource dataSource;
  private TransactionIsolationLevel level;
  private Connection connection;
  private boolean closeConnection;
  
  public ManagedTransaction(Connection connection, boolean closeConnection) {
    this.connection = connection;
    this.closeConnection = closeConnection;
  }
  public ManagedTransaction(DataSource ds, TransactionIsolationLevel level, boolean closeConnection) {
    this.dataSource = ds;
    this.level = level;
    this.closeConnection = closeConnection;
  }
  public Connection getConnection() throws SQLException {
    if (this.connection == null) {
      openConnection();
    }
    return this.connection;
  }
  public void commit() throws SQLException {
    // Does nothing
  }  
  public void rollback() throws SQLException {
    // Does nothing
  }
  
  public void close() throws SQLException {
    if (this.closeConnection && this.connection != null) {
      if (log.isDebugEnabled()) {
        log.debug("Closing JDBC Connection [" + this.connection + "]");
      }
      this.connection.close();
    }
  }
  
  protected void openConnection() throws SQLException {
    if (log.isDebugEnabled()) {
      log.debug("Opening JDBC Connection");
    }
    this.connection = this.dataSource.getConnection();
    if (this.level != null) {
      this.connection.setTransactionIsolation(this.level.getLevel());
    }
  } 
}
```

![复制代码](Mybatis%20%E4%BA%8B%E5%8A%A1%E6%8E%A7%E5%88%B6.assets/copycode.gif)

```
参考连接：
https://www.cnblogs.com/kaleidoscope/p/9707263.html?ivk_sa=1024320u
https://blog.csdn.net/weixin_34392906/article/details/91425640
```