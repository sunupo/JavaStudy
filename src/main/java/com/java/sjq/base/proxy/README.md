
# [彻底搞懂jdk动态代理并自己动手写一个动态代理](https://cloud.tencent.com/developer/article/1336135#:~:text=%E9%A6%96%E5%85%88%E6%88%91%E4%BB%AC%E5%85%88%E6%9D%A5%E8%AE%B2%E4%B8%80%E4%B8%8BJDK%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86%E7%9A%84%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86%201.%E6%8B%BF%E5%88%B0%E8%A2%AB%E4%BB%A3%E7%90%86%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%BC%95%E7%94%A8%EF%BC%8C%E7%84%B6%E5%90%8E%E8%8E%B7%E5%8F%96%E4%BB%96%E7%9A%84%E6%8E%A5%E5%8F%A3%202.JDK%E4%BB%A3%E7%90%86%E9%87%8D%E6%96%B0%E7%94%9F%E6%88%90%E4%B8%80%E4%B8%AA%E7%B1%BB%EF%BC%8C%E5%90%8C%E6%97%B6%E5%AE%9E%E7%8E%B0%E6%88%91%E4%BB%AC%E7%BB%99%E7%9A%84%E4%BB%A3%E7%90%86%E5%AF%B9%E8%B1%A1%E6%89%80%E5%AE%9E%E7%8E%B0%E7%9A%84%E6%8E%A5%E5%8F%A3,3.%E6%8A%8A%E8%A2%AB%E4%BB%A3%E7%90%86%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%BC%95%E7%94%A8%E6%8B%BF%E5%88%B0%E4%BA%86%204.%E9%87%8D%E6%96%B0%E5%8A%A8%E6%80%81%E7%94%9F%E6%88%90%E4%B8%80%E4%B8%AAclass%E5%AD%97%E8%8A%82%E7%A0%81%205.%E7%84%B6%E5%90%8E%E7%BC%96%E8%AF%91)

# jdk的动态代理
## 1.原理源码剖析
首先我们先来讲一下JDK动态代理的实现原理

1. 拿到被代理对象的引用，然后获取他的接口
2. JDK代理重新生成一个类，同时实现我们给的代理对象所实现的接口
3. 把被代理对象的引用拿到了
4. 重新动态生成一个class字节码
5. 然后编译

> 
> 在 InvocationHandler 内部 invoke 方法内调用以下方法，产生递归调用栈溢出：toString、hashCode、equals。
> 
> 当然也不能调用被代理的方法。在例子中即为 UserService.add()
> 
> 但是以下方法不会产生递归调用：notify、notifyAll、wait、getClass
> 
> 
> 
# cglib

##
```
在相同类型的不同对象上调用原始方法。
参数：
obj – 兼容对象;如果您使用传递给 MethodInterceptor 的第一个参数的对象（通常不是您想要的），则会导致递归 args – 传递给截获方法的参数;您可以替换不同的参数数组，只要类型兼容
抛出：
Throwable – 被调用方法抛出的裸异常在不包装的情况下传递 InvocationTargetException
另请参阅：
MethodInterceptor.intercept
```
```java
public Object invoke(Object obj, Object[] args) throws Throwable {}
```

## invoke
```java
在指定对象上调用原始（超级）方法。
参数：
obj – 增强的对象，必须是作为第一个参数传递给方法拦截器的对象 args – 传递给截获方法的参数;您可以替换不同的参数数组，只要类型兼容
抛出：
Throwable – 被调用方法抛出的裸异常在不包装的情况下传递 InvocationTargetException
另请参阅：
MethodInterceptor.intercept
```

```java
public Object invokeSuper(Object obj, Object[] args) throws Throwable {}
```