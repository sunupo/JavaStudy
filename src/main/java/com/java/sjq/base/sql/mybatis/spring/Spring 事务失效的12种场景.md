> [(117条消息) 聊聊Spring事务失效的12种场景，太坑人了，java工作流面试题\_didi558的博客-CSDN博客](https://blog.csdn.net/didi558/article/details/124081336?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-124081336-blog-120181443.pc_relevant_default&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-124081336-blog-120181443.pc_relevant_default&utm_relevant_index=1)
>
> [(117条消息) spring事务（注解 @Transactional ）失效的12种场景\_spring事务失效场景\_春天的早晨的博客-CSDN博客](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19)



## 前言

阿里巴巴，作为国内互联网公司的Top，算是业界的标杆，有阿里背景的程序员，也更具有权威性。作为程序员，都清楚阿里对于员工要求有多高，技术人员掌握的技术水平更是望尘莫及。所以，大厂程序员的很多经验也都值得我们借鉴和学习，在一定程度上确实能够帮助我们“走捷径”。

今天，我们要分享的是，Alibaba技术官丢出来的SpringCloud微服务实战笔记，这份笔记让人看了不得不爱，目前在GitHub的热度已经标星81.6k了，由此可见同行们对这份文档的认可程度，这也意味着对我们的学习和技术提升有很大的帮助。

> 下面将这份文档的内容以图片的形式展现出来，但篇幅有限只能展示部分，如果你需要“高清完整的pdf版”，可以在文末领取

![](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/169a7164a30f54bf175abc83d45b3360-1677253125269-95.png)

saveData(userModel);

}

}

但@[Transactional](https://so.csdn.net/so/search?q=Transactional&spm=1001.2101.3001.7020)注解，如果被加到方法上，有个缺点就是整个方法都包含在事务当中了。

上面的这个例子中，在UserService类中，其实只有这两行才需要事务：

roleService.save(userModel);

update(userModel);

在RoleService类中，只有这一行需要事务：

saveData(userModel);

现在的这种写法，会导致所有的query方法也被包含在同一个事务当中。

如果query方法非常多，调用层级很深，而且有部分查询方法比较耗时的话，会造成整个事务非常耗时，而从造成大事务问题。

关于大事务问题的危害，可以阅读一下我的另一篇文章《让人头痛的大事务问题到底要如何解决?》，上面有详细的讲解。

![聊聊Spring事务失效的12种场景，太坑人了](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/bde891a895d724f25f64f7b69328b9a8-1677253125270-99.png)

### 2.[编程式事务](https://so.csdn.net/so/search?q=%E7%BC%96%E7%A8%8B%E5%BC%8F%E4%BA%8B%E5%8A%A1&spm=1001.2101.3001.7020)

\=======

上面聊的这些内容都是基于@Transactional注解的，主要说的是它的事务问题，我们把这种事务叫做：[声明式事务](https://so.csdn.net/so/search?q=%E5%A3%B0%E6%98%8E%E5%BC%8F%E4%BA%8B%E5%8A%A1&spm=1001.2101.3001.7020)。

其实，spring还提供了另外一种创建事务的方式，即通过手动编写代码实现的事务，我们把这种事务叫做：编程式事务。例如：

```java
@Autowired

private TransactionTemplate transactionTemplate;

…

public void save(final User user) {

queryData1();

queryData2();

transactionTemplate.execute((status) => {

addData1();

updateData2();

return Boolean.TRUE;

})

}
```



在spring中为了支持编程式事务，专门提供了一个类：TransactionTemplate，在它的execute方法中，就实现了事务的功能。

相较于@Transactional注解声明式事务，我更建议大家使用，基于TransactionTemplate的编程式事务。主要原因如下：

-   避免由于spring aop问题，导致事务失效的问题。
    
-   能够更小力度地控制事务的范围，更直观。
    

建议在项目中少使用@Transactional注解开启事务。但并不是说一定不能用它，如果项目中有些业务逻辑比较简单，而且不经常变动，使用@Transactional注解开启事务开启事务也无妨，因为它更简单，开发效率更高，但是千万要小心事务失效的问题。

# 前言

\==

对于从事java开发工作的同学来说，spring的事务肯定再熟悉不过了。

在某些业务场景下，如果一个请求中，需要同时写入多张表的数据。为了保证操作的原子性(要么同时成功，要么同时失败)，避免数据不一致的情况，我们一般都会用到spring事务。

确实，spring事务用起来贼爽，就用一个简单的注解：@Transactional，就能轻松搞定事务。我猜大部分小伙伴也是这样用的，而且一直用一直爽。

但如果你使用不当，它也会坑你于无形。

今天我们就一起聊聊，事务失效的一些场景，说不定你已经中招了。不信，让我们一起看看。

![聊聊Spring事务失效的12种场景，太坑人了](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/e33dd29232e40c3763913a3de64ec972-1677253125269-93.png)

## 一 事务不生效

\=======

### 1.访问权限问题

\========

众所周知，java的访问权限主要有四种：private、default、protected、public，它们的权限从左到右，依次变大。

但如果我们在开发过程中，把有某些事务方法，定义了错误的访问权限，就会导致事务功能出问题，例如：

```java
@Service

public class UserService {

@Transactional

private void add(UserModel userModel) {

saveData(userModel);

updateData(userModel);

}

}
```



我们可以看到add方法的访问权限被定义成了private，这样会导致事务失效，spring要求被代理方法必须是public的。

说白了，在

AbstractFallbackTransactionAttributeSource类的computeTransactionAttribute方法中有个判断，如果目标方法不是public，则TransactionAttribute返回null，即不支持事务。

```java
protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {

// Don’t allow no-public methods as required.

if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {

return null;

}

// The method may be on an interface, but we need attributes from the target class.

// If the target class is null, the method will be unchanged.

Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

// First try is the method in the target class.

TransactionAttribute txAttr = findTransactionAttribute(specificMethod);

if (txAttr != null) {

return txAttr;

}

// Second try is the transaction attribute on the target class.

txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());

if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {

return txAttr;

}

if (specificMethod != method) {

// Fallback is to look at the original method.

txAttr = findTransactionAttribute(method);

if (txAttr != null) {

return txAttr;

}

// Last fallback is the class of the original method.

txAttr = findTransactionAttribute(method.getDeclaringClass());

if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {

return txAttr;

}

}

return null;

}
```



也就是说，如果我们自定义的事务方法(即目标方法)，它的访问权限不是public，而是private、default或protected的话，spring则不会提供事务功能。

### 2\. 方法用final修饰

\==============

有时候，某个方法不想被子类重新，这时可以将该方法定义成final的。普通方法这样定义是没问题的，但如果将事务方法定义成final，例如：

```
@Service

public class UserService {

@Transactional

public final void add(UserModel userModel){

saveData(userModel);

updateData(userModel);

}

}
```



我们可以看到add方法被定义成了final的，这样会导致事务失效。

为什么?

如果你看过spring事务的源码，可能会知道spring事务底层使用了aop，也就是通过jdk动态代理或者cglib，帮我们生成了代理类，在代理类中实现的事务功能。

但如果某个方法用final修饰了，那么在它的代理类中，就无法重写该方法，而添加事务功能。

注意：如果某个方法是static的，同样无法通过动态代理，变成事务方法。

### 3.方法内部调用

\========

有时候我们需要在某个Service类的某个方法中，调用另外一个事务方法，比如：

```
@Service

public class UserService {

@Autowired

private UserMapper userMapper;

@Transactional

public void add(UserModel userModel) {

userMapper.insertUser(userModel);

updateStatus(userModel);

}

@Transactional

public void updateStatus(UserModel userModel) {

doSameThing();

}

}
```



我们看到在事务方法add中，直接调用事务方法updateStatus。从前面介绍的内容可以知道，updateStatus方法拥有事务的能力是因为spring aop生成代理了对象，但是这种方法直接调用了this对象的方法，所以updateStatus方法不会生成事务。

由此可见，在同一个类中的方法直接内部调用，会导致事务失效。

那么问题来了，如果有些场景，确实想在同一个类的某个方法中，调用它自己的另外一个方法，该怎么办呢?

#### 3.1 新加一个Service方法

\=================

这个方法非常简单，只需要新加一个Service方法，把@Transactional注解加到新Service方法上，把需要事务执行的代码移到新方法中。具体代码如下：

```
@Servcie

public class ServiceA {

@Autowired

prvate ServiceB serviceB;

public void save(User user) {

queryData1();

queryData2();

serviceB.doSave(user);

}

}

@Servcie

public class ServiceB {

@Transactional(rollbackFor=Exception.class)

public void doSave(User user) {

addData1();

updateData2();

}

}
```



#### 3.2 在该Service类中注入自己

\===================

如果不想再新加一个Service类，在该Service类中注入自己也是一种选择。具体代码如下：

```
@Servcie

public class ServiceA {

@Autowired

prvate ServiceA serviceA;

public void save(User user) {

queryData1();

queryData2();

serviceA.doSave(user);

}

@Transactional(rollbackFor=Exception.class)

public void doSave(User user) {

addData1();

updateData2();

}

}
```



可能有些人可能会有这样的疑问：这种做法会不会出现循环依赖问题?

答案：不会。

其实spring ioc内部的三级缓存保证了它，不会出现循环依赖问题。但有些坑，如果你想进一步了解循环依赖问题，可以看看我之前文章《spring：我是如何解决循环依赖的?》。

#### 3.3 通过AopContext类

\=================

在该Service类中使用AopContext.currentProxy()获取代理对象

上面的方法2确实可以解决问题，但是代码看起来并不直观，还可以通过在该Service类中使用AOPProxy获取代理对象，实现相同的功能。具体代码如下：

```
@Servcie

public class ServiceA {

public void save(User user) {

queryData1();

queryData2();

((ServiceA)AopContext.currentProxy()).doSave(user);

}

@Transactional(rollbackFor=Exception.class)

public void doSave(User user) {

addData1();

updateData2();

}

}
```



### 4.未被spring管理

\============

在我们平时开发过程中，有个细节很容易被忽略。即使用spring事务的前提是：对象要被spring管理，需要创建bean实例。

通常情况下，我们通过@Controller、@Service、@Component、@Repository等注解，可以自动实现bean实例化和依赖注入的功能。

当然创建bean实例的方法还有很多，有兴趣的小伙伴可以看看我之前写的另一篇文章《@Autowired的这些骚操作，你都知道吗?》

如果有一天，你匆匆忙忙的开发了一个Service类，但忘了加@Service注解，比如：

//@Service

public class UserService {

@Transactional

public void add(UserModel userModel) {

saveData(userModel);

updateData(userModel);

}

}

从上面的例子，我们可以看到UserService类没有加@Service注解，那么该类不会交给spring管理，所以它的add方法也不会生成事务。

### 5.多线程调用

\=======

在实际项目开发中，多线程的使用场景还是挺多的。如果spring事务用在多线程场景中，会有问题吗?

@Slf4j

@Service

public class UserService {

@Autowired

private UserMapper userMapper;

@Autowired

private RoleService roleService;

@Transactional

public void add(UserModel userModel) throws Exception {

userMapper.insertUser(userModel);

new Thread(() -> {

roleService.doOtherThing();

}).start();

}

}

@Service

public class RoleService {

@Transactional

public void doOtherThing() {

System.out.println(“保存role表数据”);

}

}

从上面的例子中，我们可以看到事务方法add中，调用了事务方法doOtherThing，但是事务方法doOtherThing是在另外一个线程中调用的。

这样会导致两个方法不在同一个线程中，获取到的数据库连接不一样，从而是两个不同的事务。如果想doOtherThing方法中抛了异常，add方法也回滚是不可能的。

如果看过spring事务源码的朋友，可能会知道spring的事务是通过数据库连接来实现的。当前线程中保存了一个map，key是数据源，value是数据库连接。

private static final ThreadLocal<Map<Object, Object>> resources =

new NamedThreadLocal<>(“Transactional resources”);

我们说的同一个事务，其实是指同一个数据库连接，只有拥有同一个数据库连接才能同时提交和回滚。如果在不同的线程，拿到的数据库连接肯定是不一样的，所以是不同的事务。

### 6.表不支持事务

\========

众所周知，在mysql5之前，默认的数据库引擎是myisam。

它的好处就不用多说了：索引文件和数据文件是分开存储的，对于查多写少的单表操作，性能比innodb更好。

有些老项目中，可能还在用它。

在创建表的时候，只需要把ENGINE参数设置成MyISAM即可：

CREATE TABLE `category` (

`id` bigint NOT NULL AUTO\_INCREMENT,

`one_category` varchar(20) COLLATE utf8mb4\_bin DEFAULT NULL,

`two_category` varchar(20) COLLATE utf8mb4\_bin DEFAULT NULL,

`three_category` varchar(20) COLLATE utf8mb4\_bin DEFAULT NULL,

`four_category` varchar(20) COLLATE utf8mb4\_bin DEFAULT NULL,

PRIMARY KEY (`id`)

) ENGINE=MyISAM AUTO\_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4\_bin

myisam好用，但有个很致命的问题是：不支持事务。

如果只是单表操作还好，不会出现太大的问题。但如果需要跨多张表操作，由于其不支持事务，数据极有可能会出现不完整的情况。

此外，myisam还不支持行锁和外键。

所以在实际业务场景中，myisam使用的并不多。在mysql5以后，myisam已经逐渐退出了历史的舞台，取而代之的是innodb。

有时候我们在开发的过程中，发现某张表的事务一直都没有生效，那不一定是spring事务的锅，最好确认一下你使用的那张表，是否支持事务。

### 7.未开启事务

\=======

有时候，事务没有生效的根本原因是没有开启事务。

你看到这句话可能会觉得好笑。

开启事务不是一个项目中，最最最基本的功能吗?

为什么还会没有开启事务?

没错，如果项目已经搭建好了，事务功能肯定是有的。

但如果你是在搭建项目demo的时候，只有一张表，而这张表的事务没有生效。那么会是什么原因造成的呢?

当然原因有很多，但没有开启事务，这个原因极其容易被忽略。

如果你使用的是springboot项目，那么你很幸运。因为springboot通过

DataSourceTransactionManagerAutoConfiguration类，已经默默的帮你开启了事务。

你所要做的事情很简单，只需要配置spring.datasource相关参数即可。

但如果你使用的还是传统的spring项目，则需要在applicationContext.xml文件中，手动配置事务相关参数。如果忘了配置，事务肯定是不会生效的。

具体配置如下信息：

<tx:advice id=“advice” transaction-manager=“transactionManager”>

tx:attributes

<tx:method name=“\*” propagation=“REQUIRED”/>

</tx:attributes>

</tx:advice>

aop:config

<aop:pointcut expression=“execution(\* com.susan._._(…))” id=“pointcut”/>

<aop:advisor advice-ref=“advice” pointcut-ref=“pointcut”/>

</aop:config>

默默的说一句，如果在pointcut标签中的切入点匹配规则，配错了的话，有些类的事务也不会生效。

## 二 事务不回滚

\=======

### 1.错误的传播特性

\=========

其实，我们在使用@Transactional注解时，是可以指定propagation参数的。

该参数的作用是指定事务的传播特性，spring目前支持7种传播特性：

-   REQUIRED 如果当前上下文中存在事务，那么加入该事务，如果不存在事务，创建一个事务，这是默认的传播属性值。
    
-   SUPPORTS 如果当前上下文存在事务，则支持事务加入事务，如果不存在事务，则使用非事务的方式执行。
    
-   MANDATORY 如果当前上下文中存在事务，否则抛出异常。
    
-   REQUIRES\_NEW 每次都会新建一个事务，并且同时将上下文中的事务挂起，执行当前新建事务完成以后，上下文事务恢复再执行。
    
-   NOT\_SUPPORTED 如果当前上下文中存在事务，则挂起当前事务，然后新的方法在没有事务的环境中执行。
    
-   NEVER 如果当前上下文中存在事务，则抛出异常，否则在无事务环境上执行代码。
    
-   NESTED 如果当前上下文中存在事务，则嵌套事务执行，如果不存在事务，则新建事务。
    

如果我们在手动设置propagation参数的时候，把传播特性设置错了，比如：

@Service

public class UserService {

@Transactional(propagation = Propagation.NEVER)

public void add(UserModel userModel) {

saveData(userModel);

updateData(userModel);

}

}

我们可以看到add方法的事务传播特性定义成了Propagation.NEVER，这种类型的传播特性不支持事务，如果有事务则会抛异常。

目前只有这三种传播特性才会创建新事务：REQUIRED，REQUIRES\_NEW，NESTED。

### 2.自己吞了异常

\========

事务不会回滚，最常见的问题是：开发者在代码中手动try…catch了异常。比如：

@Slf4j

@Service

public class UserService {

@Transactional

public void add(UserModel userModel) {

try {

saveData(userModel);

updateData(userModel);

} catch (Exception e) {

log.error(e.getMessage(), e);

}

}

}

这种情况下spring事务当然不会回滚，因为开发者自己捕获了异常，又没有手动抛出，换句话说就是把异常吞掉了。

如果想要spring事务能够正常回滚，必须抛出它能够处理的异常。如果没有抛异常，则spring认为程序是正常的。

### 3.手动抛了别的异常

\==========

即使开发者没有手动捕获异常，但如果抛的异常不正确，spring事务也不会回滚。

@Slf4j

@Service

public class UserService {

@Transactional

public void add(UserModel userModel) throws Exception {

try {

saveData(userModel);

updateData(userModel);

} catch (Exception e) {

log.error(e.getMessage(), e);

throw new Exception(e);

}

}

}

上面的这种情况，开发人员自己捕获了异常，又手动抛出了异常：Exception，事务同样不会回滚。

因为spring事务，默认情况下只会回滚RuntimeException(运行时异常)和Error(错误)，对于普通的Exception(非运行时异常)，它不会回滚。

### 4.自定义了回滚异常

\==========

在使用@Transactional注解声明事务时，有时我们想自定义回滚的异常，spring也是支持的。可以通过设置rollbackFor参数，来完成这个功能。

但如果这个参数的值设置错了，就会引出一些莫名其妙的问题，例如：

@Slf4j

@Service

public class UserService {

@Transactional(rollbackFor = BusinessException.class)

public void add(UserModel userModel) throws Exception {

saveData(userModel);

updateData(userModel);

}

}

如果在执行上面这段代码，保存和更新数据时，程序报错了，抛了SqlException、DuplicateKeyException等异常。而BusinessException是我们自定义的异常，报错的异常不属于BusinessException，所以事务也不会回滚。

即使rollbackFor有默认值，但阿里巴巴开发者规范中，还是要求开发者重新指定该参数。

这是为什么呢?

因为如果使用默认值，一旦程序抛出了Exception，事务不会回滚，这会出现很大的bug。所以，建议一般情况下，将该参数设置成：Exception或Throwable。

### 5.嵌套事务回滚多了

\==========

public class UserService {

@Autowired

private UserMapper userMapper;

@Autowired

private RoleService roleService;

@Transactional

public void add(UserModel userModel) throws Exception {

userMapper.insertUser(userModel);

roleService.doOtherThing();

}

}

@Service

public class RoleService {

@Transactional(propagation = Propagation.NESTED)

public void doOtherThing() {

System.out.println(“保存role表数据”);

}

}

这种情况使用了嵌套的内部事务，原本是希望调用roleService.doOtherThing方法时，如果出现了异常，只回滚doOtherThing方法里的内容，不回滚 userMapper.insertUser里的内容，即回滚保存点。。但事实是，insertUser也回滚了。

why?

因为doOtherThing方法出现了异常，没有手动捕获，会继续往上抛，到外层add方法的代理方法中捕获了异常。所以，这种情况是直接回滚了整个事务，不只回滚单个保存点。

怎么样才能只回滚保存点呢?

@Slf4j

@Service

public class UserService {

@Autowired

private UserMapper userMapper;

@Autowired

private RoleService roleService;

@Transactional

public void add(UserModel userModel) throws Exception {

userMapper.insertUser(userModel);

try {

roleService.doOtherThing();

} catch (Exception e) {

log.error(e.getMessage(), e);

}

}

}

可以将内部嵌套事务放在try/catch中，并且不继续往上抛异常。这样就能保证，如果内部嵌套事务中出现异常，只回滚内部事务，而不影响外部事务。

## 三 其他

\====

### 1 大事务问题

\=======

在使用spring事务时，有个让人非常头疼的问题，就是大事务问题。

通常情况下，我们会在方法上@Transactional注解，填加事务功能，比如：

@Service

public class UserService {

@Autowired

private RoleService roleService;

@Transactional

public void add(UserModel userModel) throws Exception {

query1();

query2();

query3();

roleService.save(userModel);

update(userModel);

}

}

@Service

public class RoleService {

@Autowired

private RoleService roleService;

@Transactional

public void save(UserModel userModel) throws Exception {

query4();

**《一线大厂Java面试真题解析+Java核心总结学习笔记+最新全套讲解视频+实战项目源码》开源**

> **Java优秀开源项目：**
>
> -   **ali1024.coding.net/public/P7/Java/git**

## 复习的面试资料

> 这些面试全部出自大厂面试真题和面试合集当中，小编已经为大家整理完毕（PDF版）

-   **第一部分：Java基础-中级-高级**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/5aaed246c56348f85fbe68cf2231a348-1677253125270-97.png)

-   **第二部分：开源框架（SSM：Spring+SpringMVC+MyBatis）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/a2a343d0f693564cde02a5c84ca90641-1677253125270-101.png)

-   **第三部分：性能调优（JVM+MySQL+Tomcat）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/ed1498bf06278c0fa5cbc3039fd14972-1677253125270-103.png)

-   **第四部分：分布式（限流：ZK+Nginx；缓存：Redis+MongoDB+Memcached；通讯：MQ+kafka）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/2768c5c7279b091ebe98cbad0ea8bf09-1677253125270-105.png)

-   **第五部分：微服务（SpringBoot+SpringCloud+Dubbo）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/813f4ec42d898183bab5eb2993803e49-1677253125270-107.png)

-   **第六部分：其他：并发编程+设计模式+数据结构与算法+网络**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/3ed4bc401ea2af3d1fe5acd125dbc510-1677253125270-109.png)

## 进阶学习笔记pdf

> -   **Java架构进阶之架构筑基篇（**Java基础+并发编程+JVM+MySQL+Tomcat+网络+数据结构与算法**）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/31dc6f37a4b58ebfbd02d8eeea35955b-1677253125270-111.png)

-   **Java架构进阶之开源框架篇（**设计模式+Spring+SpringMVC+MyBatis**）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/d7b56999630b9740f3f6dd82c510e74d-1677253125273-113.png)

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/20ceee9df6f63ab9faacdb9959141ea5-1677253125273-117.png)

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/2a8a2607e41521d61fc4b93a5855bd95-1677253125273-115.png)

-   **Java架构进阶之分布式架构篇 （**限流（ZK/Nginx）+缓存（Redis/MongoDB/Memcached）+通讯（MQ/kafka）**）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/a67e4fbf4fb91f761657c64937ad21a8-1677253125273-119.png)

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/4a229b6ca2790d0a3b0469c25d307f03-1677253125273-121.png)

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/d2640765ef3dc064ec2d61f264bebd14-1677253125273-123.png)

-   **Java架构进阶之微服务架构篇（RPC+SpringBoot+SpringCloud+Dubbo+K8s）**

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/7023b99676931c8decb4c5335591eae8-1677253125273-125.png)

![image](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/7aa86f70139797d3ea77c547c85792e5-1677253125273-127.png)

**《一线大厂Java面试真题解析+Java核心总结学习笔记+最新全套讲解视频+实战项目源码》开源**

> **Java优秀开源项目：**
>
> -   **ali1024.coding.net/public/P7/Java/git**

## 复习的面试资料

> 这些面试全部出自大厂面试真题和面试合集当中，小编已经为大家整理完毕（PDF版）

-   **第一部分：Java基础-中级-高级**

\[外链图片转存中…(img-kXxWkc3k-1649580859409)\]

-   **第二部分：开源框架（SSM：Spring+SpringMVC+MyBatis）**

\[外链图片转存中…(img-Qpxh16eU-1649580859410)\]

-   **第三部分：性能调优（JVM+MySQL+Tomcat）**

\[外链图片转存中…(img-misovtRJ-1649580859410)\]

-   **第四部分：分布式（限流：ZK+Nginx；缓存：Redis+MongoDB+Memcached；通讯：MQ+kafka）**

\[外链图片转存中…(img-1oriMA71-1649580859410)\]

-   **第五部分：微服务（SpringBoot+SpringCloud+Dubbo）**

\[外链图片转存中…(img-UcG0Ol9g-1649580859411)\]

-   **第六部分：其他：并发编程+设计模式+数据结构与算法+网络**

\[外链图片转存中…(img-nCLSfWMW-1649580859412)\]

## 进阶学习笔记pdf

> -   **Java架构进阶之架构筑基篇（**Java基础+并发编程+JVM+MySQL+Tomcat+网络+数据结构与算法**）**

\[外链图片转存中…(img-rs1bhPK3-1649580859412)\]

-   **Java架构进阶之开源框架篇（**设计模式+Spring+SpringMVC+MyBatis**）**

\[外链图片转存中…(img-Gk2DsxRu-1649580859413)\]

\[外链图片转存中…(img-7fXeSUid-1649580859413)\]

\[外链图片转存中…(img-KSfJUmfa-1649580859414)\]

-   **Java架构进阶之分布式架构篇 （**限流（ZK/Nginx）+缓存（Redis/MongoDB/Memcached）+通讯（MQ/kafka）**）**

\[外链图片转存中…(img-cqLUQIiG-1649580859414)\]

\[外链图片转存中…(img-YasRB5aw-1649580859415)\]

\[外链图片转存中…(img-bUBNzJ8u-1649580859415)\]

-   **Java架构进阶之微服务架构篇（RPC+SpringBoot+SpringCloud+Dubbo+K8s）**

\[外链图片转存中…(img-N1w9xWjW-1649580859416)\]

\[外链图片转存中…(img-OAAVrAGN-1649580859416)\]



# ------------------------



### 文章目录

-   [一 、事务不生效【七种】](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#__6)
-   -   [1.访问权限问题 (只有public方法会生效)](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#1_public_7)
    -   [2.方法用final修饰，不会生效](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#2final_65)
    -   [3.同一个类中的方法直接内部调用，会导致事务失效](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#3_86)
    -   -   [3.1 新加一个Service方法](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#31_Service_113)
        -   [3.2 在该Service类中注入自己](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#32_Service_139)
        -   [3.3 通过AopContent类](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#33_AopContent_166)
    -   [4.(类本身) 未被spring管理](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#4_spring_187)
    -   [5.多线程调用](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#5_206)
    -   [6.(存储引擎)表不支持事务](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#6_249)
    -   [7.未开启事务](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#7_277)
-   [二、事务不回滚【五种】](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#_318)
-   -   [1.错误的传播特性](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#1_319)
    -   [2.自己吞了异常](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#2_347)
    -   [3.手动抛了别的异常](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#3_369)
    -   [4.自定义了回滚异常](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#4_392)
    -   [5.嵌套事务回滚多了](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#5_416)
-   [三、大事务与编程式事务](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#_474)
-   -   [1\. 大事务问题](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#1__475)
    -   [2\. 编程式事务](https://blog.csdn.net/mccand1234/article/details/124571619?spm=1001.2101.3001.6650.18&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-18-124571619-blog-124081336.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=19#2__528)

在某些业务场景下，如果一个请求中，需要同时写入多张表的数据或者执行多条sql。为了保证操作的原子性（要么同时成功，要么同时失败），避免数据不一致的情况，我们一般都会用到spring事务。

![强大的spring框架，为我们提供了一个简单的注解：@Transactional，就能轻松搞定事务，一直用一直爽啊！！](Spring%20%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88%E7%9A%8412%E7%A7%8D%E5%9C%BA%E6%99%AF.assets/da6f0bd7326f4bd891274e9e44fae19f.png)

## 一 、事务不生效【七种】

## 1.访问权限问题 (只有public方法会生效)

众所周知，java的访问权限主要有四种：private、default、protected、public，它们的权限从左到右，依次变大。

但如果我们在开发过程中，把有某些事务方法，定义了错误的访问权限，就会导致事务功能出问题，例如：

```
@Service
public class UserService {
    
    @Transactional
    private void add(UserModel userModel) {
         saveData(userModel);
         updateData(userModel);
    }
}
```

我们可以看到add方法的访问权限被定义成了private，这样会导致事务失效，spring要求被代理方法必须得是public的。

说白了，在AbstractFallbackTransactionAttributeSource类的computeTransactionAttribute方法中有个判断，**如果目标方法不是public**，则TransactionAttribute返回null，即不支持事务。

```
protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
    // Don't allow no-public methods as required.可以看到， 这里不支持public类型的方法
    if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
      return null;
    }

    // The method may be on an interface, but we need attributes from the target class.
    // If the target class is null, the method will be unchanged.
    Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

    // First try is the method in the target class.
    TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
    if (txAttr != null) {
      return txAttr;
    }

    // Second try is the transaction attribute on the target class.
    txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
    if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
      return txAttr;
    }

    if (specificMethod != method) {
      // Fallback is to look at the original method.
      txAttr = findTransactionAttribute(method);
      if (txAttr != null) {
        return txAttr;
      }
      // Last fallback is the class of the original method.
      txAttr = findTransactionAttribute(method.getDeclaringClass());
      if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
        return txAttr;
      }
    }
    return null;
  }
```

也就是说，如果我们自定义的事务方法（即目标方法），它的访问权限不是public，而是private、default或protected的话，spring则不会提供事务功能。

## 2.方法用final修饰，不会生效

有时候，某个方法不想被子类重新，这时可以将该方法定义成final的。普通方法这样定义是没问题的，但如果将事务方法定义成final，例如：

```
@Service
public class UserService {

    @Transactional
    public final void add(UserModel userModel){
        saveData(userModel);
        updateData(userModel);
    }
}
```

我们可以看到add方法被定义成了final的，这样会导致[事务失效](https://so.csdn.net/so/search?q=%E4%BA%8B%E5%8A%A1%E5%A4%B1%E6%95%88&spm=1001.2101.3001.7020)。

为什么？

如果你看过[spring事务](https://so.csdn.net/so/search?q=spring%E4%BA%8B%E5%8A%A1&spm=1001.2101.3001.7020)的源码，可能会知道spring事务底层使用了aop，也就是通过jdk动态代理或者cglib，帮我们生成了代理类，在代理类中实现的事务功能。**但如果某个方法用final修饰了，那么在它的代理类中，就无法重写该方法**，而添加事务功能。

注意：如果某个方法是static的，同样无法通过动态代理，变成事务方法。

## 3.同一个类中的方法直接内部调用，会导致事务失效

有时候我们需要在某个Service类的某个方法中，调用另外一个事务方法，比如：

```
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

  
    public void add(UserModel userModel) {
        userMapper.insertUser(userModel);
        updateStatus(userModel);
    }

    @Transactional
    public void updateStatus(UserModel userModel) {
        doSameThing();
    }
}
```

我们看到在事务方法add中，直接调用事务方法updateStatus。从前面介绍的内容可以知道，updateStatus方法拥有事务的能力是因为spring aop生成代理了对象，但是这种方法直接调用了this对象的方法，所以updateStatus方法不会生成事务。[根本原因](https://blog.csdn.net/yyoc97/article/details/81911744) [根本原因2](https://blog.csdn.net/mccand1234/article/details/124578233)

由此可见，在同一个类中的方法直接内部调用，会导致事务失效。

那么问题来了，如果有些场景，确实想在同一个类的某个方法中，调用它自己的另外一个方法，该怎么办呢？

### 3.1 新加一个Service方法

这个方法非常简单，只需要新加一个Service方法，把@Transactional注解加到新Service方法上，把需要事务执行的代码移到新方法中。具体代码如下：

```
@Servcie
public class ServiceA {
   @Autowired
   prvate ServiceB serviceB;

   public void save(User user) {
         queryData1();
         queryData2();
         serviceB.doSave(user);
   }
 }

 @Servcie
 public class ServiceB {

    @Transactional(rollbackFor=Exception.class)
    public void doSave(User user) {
       addData1();
       updateData2();
    }

 }
```

### 3.2 在该Service类中注入自己

如果不想再新加一个Service类，在该Service类中注入自己也是一种选择。具体代码如下：

```
@Servcie
public class ServiceA {
   @Autowired
   prvate ServiceA serviceA;

   public void save(User user) {
         queryData1();
         queryData2();
         serviceA.doSave(user);
   }

   @Transactional(rollbackFor=Exception.class)
   public void doSave(User user) {
       addData1();
       updateData2();
    }
 }
```

可能有些人可能会有这样的疑问：这种做法会不会出现循环依赖问题？

答案：不会。

其实spring ioc内部的[三级缓存](https://blog.csdn.net/mccand1234/article/details/116403266)保证了它，不会出现循环依赖问题。

### 3.3 通过AopContent类

在该Service类中使用AopContext.currentProxy()获取代理对象

上面的方法2确实可以解决问题，但是代码看起来并不直观，还可以通过在该Service类中使用AOPProxy获取代理对象，实现相同的功能。具体代码如下：

```
@Servcie
public class ServiceA {

   public void save(User user) {
         queryData1();
         queryData2();
         ((ServiceA)AopContext.currentProxy()).doSave(user);
   }

   @Transactional(rollbackFor=Exception.class)
   public void doSave(User user) {
       addData1();
       updateData2();
    }
 }
```

## 4.(类本身) 未被spring管理

在我们平时开发过程中，有个细节很容易被忽略。即使用spring事务的前提是：对象要被spring管理，需要创建bean实例。

通常情况下，我们通过@Controller、@Service、@Component、@Repository等注解，可以自动实现bean实例化和依赖注入的功能。当然创建bean实例的方法还有很多，不一一说了。有兴趣的小伙伴可以参考这篇文章：@Autowired的这些骚操作，你都知道吗？

如下所示, 开发了一个Service类，但忘了加@Service注解，比如：

```
//@Service
public class UserService {

    @Transactional
    public void add(UserModel userModel) {
         saveData(userModel);
         updateData(userModel);
    }    
}
```

从上面的例子，我们可以看到UserService类没有加@Service注解，那么该类不会交给spring管理，所以它的add方法也不会生成事务。

## 5.多线程调用

在实际项目开发中，多线程的使用场景还是挺多的。如果spring事务用在多线程场景中，会有问题吗？

```
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleService roleService;

    @Transactional
    public void add(UserModel userModel) throws Exception {
        userMapper.insertUser(userModel);
        new Thread(() -> {
            roleService.doOtherThing();
        }).start();
    }
}

@Service
public class RoleService {

    @Transactional
    public void doOtherThing() {
        System.out.println("保存role表数据");
    }
}
```

从上面的例子中，我们可以看到事务方法add中，调用了事务方法doOtherThing，但是事务方法doOtherThing是在另外一个线程中调用的。

这样会导致两个方法不在同一个线程中，获取到的数据库连接不一样，从而是两个不同的事务。如果想doOtherThing方法中抛了异常，add方法也回滚是不可能的。

如果看过spring事务源码的朋友，可能会知道spring的事务是通过数据库连接来实现的。当前线程中保存了一个map，key是数据源，value是数据库连接。

```
private static final ThreadLocal<Map<Object, Object>> resources =

  new NamedThreadLocal<>("Transactional resources");

```

我们说的同一个事务，其实是指同一个数据库连接，只有拥有同一个数据库连接才能同时提交和回滚。如果在不同的线程，拿到的数据库连接肯定是不一样的，所以是不同的事务。

## 6.(存储引擎)表不支持事务

周所周知，在mysql5之前，默认的数据库引擎是myisam。

它的好处就不用多说了：索引文件和数据文件是分开存储的，对于查多写少的单表操作，性能比innodb更好。

有些老项目中，可能还在用它。

在创建表的时候，只需要把ENGINE参数设置成MyISAM即可：

```
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `one_category` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `two_category` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `three_category` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `four_category` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
```

myisam好用，但有个很致命的问题是：**不支持事务**。

如果只是单表操作还好，不会出现太大的问题。但如果需要跨多张表操作，由于其不支持事务，数据极有可能会出现不完整的情况。

此外，myisam还不支持行锁和外键。

所以在实际业务场景中，myisam使用的并不多。在mysql5以后，myisam已经逐渐退出了历史的舞台，取而代之的是innodb。

有时候我们在开发的过程中，发现某张表的事务一直都没有生效，那不一定是spring事务的锅，最好确认一下你使用的那张表，是否支持事务。

## 7.未开启事务

有时候，事务没有生效的根本原因是没有开启事务。

你看到这句话可能会觉得好笑。

开启事务不是一个项目中，最最最基本的功能吗？

为什么还会没有开启事务？

没错，如果项目已经搭建好了，事务功能肯定是有的。

但如果你是在搭建项目demo的时候，只有一张表，而这张表的事务没有生效。那么会是什么原因造成的呢？

当然原因有很多，但没有开启事务，这个原因极其容易被忽略。

如果你使用的是springboot项目，那么你很幸运。因为springboot通过DataSourceTransactionManagerAutoConfiguration类，已经默默的帮你开启了事务。

你所要做的事情很简单，只需要配置spring.datasource相关参数即可。

但如果你使用的还是传统的spring项目，则需要在applicationContext.xml文件中，手动配置事务相关参数。如果忘了配置，事务肯定是不会生效的。

具体配置如下信息：

```
<!-- 配置事务管理器 --> 
<bean class="org.springframework.jdbc.datasource.DataSourceTransactionManager" id="transactionManager"> 
    <property name="dataSource" ref="dataSource"></property> 
</bean> 
<tx:advice id="advice" transaction-manager="transactionManager"> 
    <tx:attributes> 
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes> 
</tx:advice> 
<!-- 用切点把事务切进去 --> 
<aop:config> 
    <aop:pointcut expression="execution(* com.susan.*.*(..))" id="pointcut"/> 
    <aop:advisor advice-ref="advice" pointcut-ref="pointcut"/> 
</aop:config> 

```

默默的说一句，如果在pointcut标签中的切入点匹配规则，配错了的话，有些类的事务也不会生效。

## 二、事务不回滚【五种】

## 1.错误的传播特性

其实，我们在使用@Transactional注解时，是可以指定propagation参数的。

该参数的作用是指定事务的传播特性，spring目前支持7种传播特性：

REQUIRED 如果当前上下文中存在事务，那么加入该事务，如果不存在事务，创建一个事务，这是默认的传播属性值。  
SUPPORTS 如果当前上下文存在事务，则支持事务加入事务，如果不存在事务，则使用非事务的方式执行。  
MANDATORY 如果当前上下文中存在事务，否则抛出异常。  
REQUIRES\_NEW 每次都会新建一个事务，并且同时将上下文中的事务挂起，执行当前新建事务完成以后，上下文事务恢复再执行。  
NOT\_SUPPORTED 如果当前上下文中存在事务，则挂起当前事务，然后新的方法在没有事务的环境中执行。  
NEVER 如果当前上下文中存在事务，则抛出异常，否则在无事务环境上执行代码。  
NESTED 如果当前上下文中存在事务，则嵌套事务执行，如果不存在事务，则新建事务。  
如果我们在手动设置propagation参数的时候，把传播特性设置错了，比如：

```
@Service
public class UserService {

    @Transactional(propagation = Propagation.NEVER)
    public void add(UserModel userModel) {
        saveData(userModel);
        updateData(userModel);
    }
}
```

我们可以看到add方法的事务传播特性定义成了Propagation.NEVER，这种类型的传播特性不支持事务，如果有事务则会抛异常。

目前只有这三种传播特性才会创建新事务：REQUIRED，REQUIRES\_NEW，NESTED。

## 2.自己吞了异常

事务不会回滚，最常见的问题是：开发者在代码中手动try…catch了异常。比如：

```
@Slf4j
@Service
public class UserService {
    
    @Transactional
    public void add(UserModel userModel) {
        try {
            saveData(userModel);
            updateData(userModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
```

这种情况下spring事务当然不会回滚，因为开发者**自己捕获了异常**，又没有手动抛出，换句话说就是把异常吞掉了。

如果想要spring事务能够正常回滚，必须抛出它能够处理的异常。如果没有抛异常，则spring认为程序是正常的。

## 3.手动抛了别的异常

即使开发者没有手动捕获异常，但如果抛的异常不正确，spring事务也不会回滚。

```
@Slf4j
@Service
public class UserService {
    
    @Transactional
    public void add(UserModel userModel) throws Exception {
        try {
             saveData(userModel);
             updateData(userModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
```

上面的这种情况，开发人员自己捕获了异常，又手动抛出了异常：Exception，事务同样不会回滚。  

因为spring事务，默认情况下只会回滚RuntimeException（运行时异常）和Error（错误），对于普通的Exception（[非运行时异常](https://blog.csdn.net/mccand1234/article/details/51579425?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522165164871416780357228953%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fblog.%2522%257D&request_id=165164871416780357228953&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_ecpm_v1~rank_v31_ecpm-1-51579425.nonecase&utm_term=error&spm=1018.2226.3001.4450)），它不会回滚。比如常见的IOExeption和SQLException

## 4.自定义了回滚异常

在使用@Transactional注解声明事务时，有时我们想自定义回滚的异常，spring也是支持的。可以通过设置rollbackFor参数，来完成这个功能。

但如果这个参数的值设置错了，就会引出一些莫名其妙的问题，例如：

```
@Slf4j
@Service
public class UserService {
    
    @Transactional(rollbackFor = BusinessException.class)
    public void add(UserModel userModel) throws Exception {
       saveData(userModel);
       updateData(userModel);
    }
}
```

如果在执行上面这段代码，保存和更新数据时，程序报错了，抛了SqlException、DuplicateKeyException等异常。而BusinessException是我们自定义的异常，报错的异常不属于BusinessException，所以事务也不会回滚。

即使rollbackFor有默认值，但阿里巴巴开发者规范中，还是要求开发者重新指定该参数。

这是为什么呢？

因为如果使用默认值，一旦程序抛出了Exception，事务不会回滚，这会出现很大的bug。所以，建议一般情况下，将该参数设置成：Exception或Throwable。

## 5.嵌套事务回滚多了

```
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Transactional
    public void add(UserModel userModel) throws Exception {
        userMapper.insertUser(userModel);
        roleService.doOtherThing();
    }
}

@Service
public class RoleService {

    @Transactional(propagation = Propagation.NESTED)
    public void doOtherThing() {
        System.out.println("保存role表数据");
    }
}
```

这种情况使用了嵌套的内部事务，原本是希望调用roleService.doOtherThing方法时，如果出现了异常，只回滚doOtherThing方法里的内容，不回滚 userMapper.insertUser里的内容，即回滚保存点。但事实是，insertUser也回滚了。

why?

因为doOtherThing方法出现了异常，**没有手动捕获**，会继续往上抛，到外层add方法的代理方法中捕获了异常。所以，这种情况是直接回滚了整个事务，不只回滚单个保存点。

怎么样才能只回滚保存点呢？

```
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Transactional
    public void add(UserModel userModel) throws Exception {

        userMapper.insertUser(userModel);
        try {
            roleService.doOtherThing();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
```

可以将内部嵌套事务放在try/catch中，并且不继续往上抛异常。这样就能保证，如果内部嵌套事务中出现异常，只回滚内部事务，而不影响外部事务。

## 三、大事务与编程式事务

## 1\. 大事务问题

在使用spring事务时，有个让人非常头疼的问题，就是大事务问题。  
关于大事务可参考：[大事务问题](https://blog.csdn.net/mccand1234/article/details/124560532)

通常情况下，我们会在方法上@Transactional注解，填加事务功能，比如：

```
@Service
public class UserService {
    
    @Autowired 
    private RoleService roleService;
    
    @Transactional
    public void add(UserModel userModel) throws Exception {
       query1();
       query2();
       query3();
       roleService.save(userModel);
       update(userModel);
    }
}


@Service
public class RoleService {
    
    @Autowired 
    private RoleService roleService;
    
    @Transactional
    public void save(UserModel userModel) throws Exception {
       query4();
       query5();
       query6();
       saveData(userModel);
    }
}
```

但@Transactional注解，如果被加到方法上，有个缺点就是整个方法都包含在事务当中了。

上面的这个例子中，在UserService类中，其实只有这两行才需要事务：

```
roleService.save(userModel);
update(userModel);
```

在RoleService类中，只有这一行需要事务：

```
saveData(userModel);
```

现在的这种写法，会导致所有的query方法也被包含在同一个事务当中。

如果query方法非常多，调用层级很深，而且有部分查询方法比较耗时的话，会造成整个事务非常耗时，而从造成大事务问题。

## 2\. 编程式事务

上面的这些内容都是基于@Transactional注解的，主要讲的是它的事务问题，我们把这种事务叫做：声明式事务。

其实，spring还提供了另外一种创建事务的方式，即通过手动编写代码实现的事务，我们把这种事务叫做：编程式事务。例如：

```
   @Autowired
   private TransactionTemplate transactionTemplate;
   
   public void save(final User user) {
         queryData1();
         queryData2();
         transactionTemplate.execute(transactionStatus -> {
            addData1();
            updateData2();
            return Boolean.TRUE;
        });
   }
```

在spring中为了支持编程式事务，专门提供了一个类：TransactionTemplate，在它的execute方法中，就实现了事务的功能。

相较于@Transactional注解声明式事务，更建议大家使用，基于TransactionTemplate的编程式事务。主要原因如下：

避免由于spring aop问题，导致事务失效的问题。  
能够更小粒度的控制事务的范围，更直观。  
建议在项目中少使用@Transactional注解开启事务。但并不是说一定不能用它，如果项目中有些业务逻辑比较简单，而且不经常变动，使用@Transactional注解开启事务开启事务也无妨，因为它更简单，开发效率更高，但是千万要小心事务失效的问题。

参考:  
https://blog.csdn.net/wang\_luwei/article/details/121549005 (spring事务（注解 @Transactional ）失效的12种场景)  
https://www.bilibili.com/video/BV1fR4y1u7N8?spm\_id\_from=333.337.search-card.all.click(分析spring事务@Transactional注解,事务不生效的场景及原因，和解决方案)