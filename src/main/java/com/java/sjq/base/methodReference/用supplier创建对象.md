> 作者专注于Java、架构、Linux、小程序、爬虫、自动化等技术。 工作期间含泪整理出一些资料，微信搜索【程序员高手之路】，回复 【java】【黑客】【爬虫】【小程序】【面试】等关键字免费获取资料。 

# 一、官方给的接口

使用**FunctionalInterface**注解修饰接口，只有一个get方法

```java
@FunctionalInterface
public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
```

# 二、解析

如下列代码所示：

使用Supplier创建对象，语法结构：

## 无参数：

1\. Supplier<T> instance = T::new;

2. Supplier<T> instance = () -> new T();

## 有参数：

1. Function<String, T> fun = T::new;  
    fun.apply("test");

2\. Function<String, T> fun2 = str -> new T(str);  
    fun2.apply("test2");

注：每次调用get方法都会创建一个对象，下面的代码中调用了两次get方法，打印的hashcode是不一样的！

```java
public class TestSupplier {
	public static void main(String[] args) {
		//无参数1：
		Supplier<TestSupplier> sup = TestSupplier::new;
		sup.get();
		sup.get();
		//无参数2：
		Supplier<TestSupplier> sup2 = () -> new TestSupplier();
		sup2.get();
		sup2.get();
		
		//有参数1：
		Function<String, TestSupplier> fun = TestSupplier::new;
		fun.apply("test");
		//有参数2：
		Function<String, TestSupplier> fun2 = str -> new TestSupplier(str);
		fun2.apply("test2");
	}
 
	public TestSupplier() {
		System.out.println(this.hashCode());
	}
	
	public TestSupplier(String str) {
		System.out.println(this.hashCode() + "，参数：" + str);
	}
}
```