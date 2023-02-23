# 一、[一文学会使用MethodHandle](https://blog.csdn.net/yuge1123/article/details/107444857#:~:text=MethodHandles%201%20%E6%9F%A5%E6%89%BE%E6%96%B9%E6%B3%95%EF%BC%8C%E4%BB%A5%E5%8F%8A%E5%8F%AF%E4%BB%A5%E5%B8%AE%E5%8A%A9%E5%AD%97%E6%AE%B5%E6%88%96%E8%80%85%E6%96%B9%E6%B3%95%E5%88%9B%E5%BB%BA%E7%9B%B8%E5%BA%94%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%20%28MethodHandle%29,2%20%E7%BB%84%E5%90%88%E5%99%A8%E6%96%B9%E6%B3%95%EF%BC%8C%E7%94%A8%E4%BA%8E%E5%B0%86%E7%8E%B0%E6%9C%89%E7%9A%84%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%BB%84%E5%90%88%E6%88%96%E8%BD%AC%E6%8D%A2%E6%88%90%E6%96%B0%E7%9A%84%E6%96%B9%E6%B3%95%203%20%E7%94%A8%E4%BA%8E%E5%88%9B%E5%BB%BA%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%9A%84%E5%85%B6%E4%BB%96%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95%E5%8F%AF%E4%BB%A5%E6%A8%A1%E6%8B%9F%E5%85%B6%E4%BB%96%E5%B8%B8%E8%A7%81%E7%9A%84JVM%E6%93%8D%E4%BD%9C%E6%88%96%E6%8E%A7%E5%88%B6%E6%B5%81%E6%A8%A1%E5%BC%8F)
### MethodHandle方法句柄

第一次看到这个类是在看[Mybatis](https://so.csdn.net/so/search?q=Mybatis&spm=1001.2101.3001.7020)源码，自以为对java很熟悉，反射很熟的我，看到这个API竟然不知道是干什么用的

因此花了很多时间去了解这个东西，发现网上的资料是很少的，特别是对刚接触这个api的人很不友好，后来通过自己的不断验证，不断查找资料，终于有了一些自己的了解，但还是不是很深，因为这套Api涉及到jVM指令层去了

### MethodHandles

这个类有一下三点作用

1.  查找方法，以及可以帮助字段或者方法创建相应的方法句柄(MethodHandle)
2.  组合器方法，用于将现有的方法句柄组合或转换成新的方法
3.  用于创建方法句柄的其他工厂方法可以模拟其他常见的JVM操作或控制流模式

PS: 很强大有木有

我们想要得到方法句柄对象(MethodHandle)要通过MethodHandles才能获取 (不要搞混了，一个是MethodHandle方法句柄对象，一个是MethodHandles 用来帮助我们获取方法句柄对象的)

### MethodHandles常用方法

```
MethodHandles.lookup();
MethodHandles.publicLookup();
```

这两个方法都会返回一个 MethodHandles.Lookup 类型的对象

##### 明确一个事实

MethodHandle不如反射强大，比如方法能够获取类的所有信息，或者方法的所有信息，但是MethodHandle是获取不到这么多信息的。

但是MethodHandle在比操纵方法这一方面是比反射要强大很多的。

这俩个方法的区别在于一个可以为任意权限的字段或者方法构造一个方法句柄，一个只能为 public 修饰的方法或者字段构造一个方法句柄

就算MethodHandles.lookup() 可以为任何访问权限的方法或者字段构造一个方法句柄  
但是也无法通过MethodHandles.Lookup对象进行查找你本身不具有权限访问的方法或者字段。

举例： 一个方法是private修饰的，那么除了方法本身所有的类具有访问权限之外其他类是无法访问的，同理如果使用MethodHandles.Lookup对象进行方法查找时，如果要查找一个private修饰的方法，除非该方法在你使用MethodHandles.Lookup这个对象的本身，即在定义这个方法的类中使用，否则则无法查找到这个方法。这里的访问权限和正常写代码的权限一样，即你在写代码的时候可以在这个方法直接调用另一个方法的话那么MethodHandles.Lookup对象也能查找到，反之则查找不到。

我偏要其他类中的private修饰或者其他我没权限访问的方法和字段怎么办？我偏要，这也是个很常见的需求。

MethodHandles.Lookup可以与反射相结合，即给他一个Field对象或者Method对象就能给你返回一个方法句柄对象

`MethodHandles.publicLookup();`  
只能构造public修饰的方法或者字段的方法句柄，即使你通过反射或者到该方法再进行构造也不行，总之只能构造public修饰的

MethodHandles.Lookup可以产生对方法、字段、构造函数的方法句柄

### MethodHandles中静态工厂方法创建通用的方法句柄

`MethodHandle arrayElementGetter(Class<?> arrayClass)`

`MethodHandle arrayElementSetter(Class<?> arrayClass)`

用来创建操作数组的方法句柄

```
        int[] arr = new int[]{1, 2, 3, 4, 5, 6};

        MethodHandle getter = MethodHandles.arrayElementGetter(int[].class);
        MethodHandle setter = MethodHandles.arrayElementSetter(int[].class);
        setter.bindTo(arr).invoke(2, 50);
        System.out.println(getter.bindTo(arr).invoke(2));
        System.out.println(Arrays.toString(arr));

```

`MethodHandle identity(Class<?> type)`

该方法总是返回你给定的值，即你传的参数是什么就返回什么

```
        MethodHandle identity = MethodHandles.identity(String.class);
        System.out.println(identity.invoke("hello world"));
        
```

`MethodHandle constant(Class<?> type, Object value)`

与上面那个方法不同的是，该方法在创建方法句柄的时候就指定一个值，然后每次调用这个方法句柄的时候都会返回这个值

```
        MethodHandle helloWorld = MethodHandles.constant(String.class, "hello world");
        System.out.println(helloWorld.invoke());
```

`MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes)`

可以简单理解为在调用的时候忽略掉哪些位置上的参数

```

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.findStatic(TestMain.class, "test", MethodType.methodType(int.class, int.class));
        // 忽略第0个参数并且类型为int类型的参数
        methodHandle = MethodHandles.dropArguments(methodHandle, 0, int.class);
        // 实际传递的只有3 
        methodHandle.invoke(2, 3);
    }


    public static int test(int i) {
        System.out.println(i);
        return 3;
    }

```

`MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters)`

可以在方法调用的时候对参数进行处理

```

    public static void main(String[] args) throws Throwable {

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle length = lookup.findVirtual(String.class, "length", MethodType.methodType(int.class));
        MethodHandle test = lookup.findStatic(TestMain.class, "test", MethodType.methodType(int.class, int.class));
        test = MethodHandles.filterArguments(test, 0, length);
        // test()方法实际接收到的参数是5
        test.invoke("sdfsd");
    }


    public static int test(int i) {
        System.out.println(i);
        return 3;
    }

```

上面这个例子，test方法接收的是一个int类型的参数，但是我们传递的是一个字符串。因此我们把参数进行了一个处理 `test = MethodHandles.filterArguments(test, 0, length);`这行代码就是表示，test方法句柄调用的时候调用length方法句柄进行处理。

`MethodHandle insertArguments(MethodHandle target, int pos, Object... values)`

给指定位置上的参数预先绑定一个值，这样在调用的时候就不能传了

```
    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.findStatic(TestMain.class, "test", MethodType.methodType(int.class, int.class));
        // 预先给指定位置的参数绑定一个值 
        methodHandle = MethodHandles.insertArguments(methodHandle, 0, 22);
        // 由于参数i已经绑定值了，在这里调用的时候就不能传递参数了
        methodHandle.invoke();
    }


    public static int test(int i) {
        System.out.println(i);
        return 3;

```

`MethodHandle foldArguments(MethodHandle target, MethodHandle combiner)`

与上面的方法类似，不同的是该方法不是在指定位置绑定值，而是通过一个方法句柄的返回值，将该返回值放到最终在调用方法的前面

```

    public static void main(String[] args) throws Throwable {

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.findStatic(TestMain.class, "test", MethodType.methodType(int.class, int.class, int.class, int.class, int.class));
        MethodHandle max = lookup.findStatic(Math.class, "max", MethodType.methodType(int.class, int.class, int.class));
        methodHandle = MethodHandles.foldArguments(methodHandle, max);
        methodHandle.invoke(4, 5, 6);
    }


    public static int test(int i, int i2, int i3, int i4) {
        // 打印5
        System.out.println(i);
        return 3;
    }


```

`MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler)`

如果terget方法句柄出现了指定的异常或其指定的子类异常，则调用handler方法

```
    public static void main(String[] args) throws Throwable {

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.findStatic(TestMain.class, "test", MethodType.methodType(int.class, int.class, int.class, int.class, int.class));
        MethodHandle exceptionHandle = lookup.findStatic(TestMain.class, "handleException", MethodType.methodType(int.class, Exception.class, int.class, int.class, int.class, int.class));
        methodHandle = MethodHandles.catchException(methodHandle, Exception.class, exceptionHandle);
        methodHandle.invoke(4, 5, 6, 7);
    }


    public static int test(int i, int i2, int i3, int i4) {
        System.out.println(i);
        throw new RuntimeException("test出现异常");
    }

    public static int handleException( Exception e, int i, int i2, int i3, int i4) {
        System.out.println("handleException:\n" + e.getMessage());
        return 2;
    }
    
```

值得注意的是 handleException方法的 异常类型参数只能在第一个位置，然后其他参数必须与出现异常方法的参数类型一致

`MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType)`

构造出一个只抛出异常的方法句柄

```
        MethodHandle handle = MethodHandles.throwException(String.class, Exception.class);
        String invoke = (String) handle.invoke(new RuntimeException("throw"));
```

MethodHandles中的方法就先讲这么多 PS: 本人比较懒…

### MethodType

该类能够产生对方法的描述即 `(Ljava/lang/Object;)V` 该方法接收一个Object类型的值，没有返回值

其实该类就是用来对方法的描述，描述这个方法接受什么参数，返回什么类型的值

```
MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes)

MethodType genericMethodType(int objectArgCount, boolean finalArray) 

MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader)

```

想要获得MethodType对象，MethodType类提供了三个静态方法，如上所述。

`MethodType.methodType(Class<?>, Class<?>, Class<?>...)` 第一个参数代表返回类型，如果没有则指定`void.class`即可,后面的参数都是这个方法接收的参数类型，可以有多个，也可以没有，MethodType.methodType()有多个重载方法

`MethodType genericMethodType(int objectArgCount, boolean finalArray)` 生成一个MethodType，第一个参数表示要生成的参数个数，并且都是Object类型，第二个参数表示是否要在最后再添加一个Object类型的数组，注意是添加哦

`MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader)` 从方法描述符来生成一个MethodType， 第二个参数为一个类加载器，如果为null则使用系统类加载器

`MethodType.fromMethodDescriptorString("(IJLjava/lang/String;)Ljava/lang/String;", null);` 生成一个 接受int、long和String类型的参数，返回一个String类型

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200719142839435.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3l1Z2UxMTIz,size_16,color_FFFFFF,t_70)

### MethodType参数的修改

获得一个具体MethodType实例后，我们可以对它进行一些修改，比如更改参数类型，添加一个参数，删除一个参数等等，但是由于MethodType本身是不可变的，所以每对其进行一次更改都会产生一个新的MethodType对象

```

// 在方法后面追加多个参数
MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) 
// 在方法后买你追加一个参数
MethodType appendParameterTypes(Class<?>... ptypesToInsert)
// 在指定参数位置插入一个参数 从 0开始
MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert)
// 在指定参数位置插入多个参数 从 0开始
MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert)
// 改变返回值类型
MethodType changeReturnType(Class<?> nrtype)
// 改变指定参数位置的参数类型
MethodType changeParameterType(int num, Class<?> nptype)
// 把基本类型变成对应的包装类型 (装箱)
MethodType wrap()
// 把包装类型变成对应的基本类型(拆箱)
MethodType unwrap()
// 把所有引用类型的参数变为Object类型
MethodType erase()
// 把所有参数都变成Object类型
MethodType generic()

```

```
        // 构造出一个 （int,long,String)String
        MethodType methodType = MethodType.fromMethodDescriptorString("(IJLjava/lang/String;)Ljava/lang/String;", null);
        // （double, long, String)String
        methodType = methodType.changeParameterType(0, Double.TYPE);
        // (double, long, String, Object)String
        methodType = methodType.appendParameterTypes(Object.class);
        // (boolean, double, long, String, Object)String
        methodType = methodType.insertParameterTypes(0, Boolean.TYPE);
        // (float, double, long, String, Object)String
        methodType = methodType.changeParameterType(0, Float.TYPE);
        // (float, double, long, String, Object)Object
        methodType = methodType.changeReturnType(Object.class);
        // (Float, Double, Long, String, Object)Object
        methodType = methodType.wrap();
        // (float, double, long, String, Object)Object
        methodType = methodType.unwrap();
        // (float, double, long, Object, Object)Object
        methodType = methodType.erase();
        // (Object, Object, Object, Object, Object)Object
        methodType = methodType.generic();

        List<Class<?>> classList =  methodType.parameterList();
        for (Class<?> clazz : classList) {
            System.out.println(clazz.getName());
        }

        
```

### MethodHandle (方法句柄)

可以通过MethodType和MethodHandles.Lookup对象产生一个方法句柄对象了

```
// 所有方法都会查找指定类中的指定方法，如果查找到了
// 则会返回这个方法的方法句柄

// 返回指定类的指定构造函数
public MethodHandle findConstructor(Class<?> refc,
                                    MethodType type);
 
// 查找虚方法 final修饰的也可找到                                  
public MethodHandle findVirtual(Class<?> refc,
                                String name,
                                MethodType type);
// 通过反射获取方法句柄
public MethodHandle unreflect(Method m);

// 查找静态方法
public MethodHandle findStatic(Class<?> refc,
                               String name,
                               MethodType type);

// 查找某个字段，并生成get方法的方法句柄(类中不需要存在这个字段的get方法)
ublic MethodHandle findGetter(Class<?> refc,
                               String name,
                               Class<?> type)

// 查找某个字段，并生成set方法的方法句柄（类中不需要存在这个字段的set方法）
public MethodHandle findSetter(Class<?> refc,
                               String name,
                               Class<?> type)
```

提一嘴：可以简单理解为虚方法指的是可以被子类覆盖的方法

上面的方法都能构造一个相对应的方法句柄对象

大概就说一说这几个方法，其他方法都与这几个方法类似，把这几个方法理解了，其他几个方法也就都差不多了。

### MethodHandle中的方法

```
    MethodHandle bindTo(Object x)
    Objectinvoke(Object... args)
    ObjectinvokeExact(Object... args)
    ObjectinvokeWithArguments(Object... arguments)
```

只讲上面这几个简单常用的方法

`MethodHandle bindTo(Object x)` 把一个对象与绑定并返回绑定后的方法句柄

`Object invoke(Object... args)` 调用这个方法句柄绑定的方法，如果该句柄没有绑定对象(没调用 bindTo()方法)并且该句柄绑定的方法是实例方法，则第一参数必须是方法所在的对象，后面的参数为给方法的参数

`Object invokeExact(Object... args)` 精确调用，对返回值，参数等要求极高，该方法不会尝试给程序员做类型转换，而其他两个调用的方法则会尝试给你做类型转换

`Object invokeWithArguments(Object... arguments)` 该方法与 invoke 方法类似，只是传递参数的方式有所不同

PS: 上面三个调用方法都一样，如果没有绑定对象，则一个参数一定要是该方法所在类的对象实例

##### 这三个方法的区别

`Object invokeExact(Object... args)` 方法对参数类型以及返回值的类型要求极其严格

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200719143212475.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3l1Z2UxMTIz,size_16,color_FFFFFF,t_70)

这种情况下是能正常执行了(思考一下 test方法明明只接收一个 类型为 A的对象而我调用的时候为什么要穿两个对象？ 答案就在上面)

接下来我们稍微改变一下下

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200719143200269.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3l1Z2UxMTIz,size_16,color_FFFFFF,t_70)

只将我们传的对象变成Object的引用就报错了

`Object invokeExact(Object... args)` 对参数及返回值极其严格

思考：用这种方式，如果方法返回一个String类型的值 我用Object类型接收会不会报错

而这种方式使用`Object invokeExact(Object... args)`和`Object invokeWithArguments(Object... arguments)`都不会有问题

那么`Object invokeExact(Object... args)`和`Object invokeWithArguments(Object... arguments)`区别在哪？

这个两个的区别就在调用方法的时候传递参数有区别，且在传递数组参数的时候更能体现出来

```

        MethodType methodType = MethodType.methodType(void.class, A.class, A.class);
        MethodHandle test = lookup.findVirtual(CommonUtil.class, "test02", methodType);
        A[] obj = { new A(), new A() };
        MethodHandle methodHandle = test.bindTo(commonUtil);
        methodHandle.invokeWithArguments(obj);


    public void test02(A a, A a1) {
        System.out.println("test02");
    }

```

上面这段代码使用`methodHandle.invokeWithArguments(obj);`调用能够正常执行，但是如果使用`methodHandle.invoke(obj)`就会执行报错

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200719143018753.png)

很简单，`methodHandle.invoke(obj)`直接把整个数组当成一个参数传递，而`methodHandle.invokeWithArguments(obj);`则会把数组中的元素一个一个传递而不是像invoke那样直接当做一个整体

### 实际操作

通过代码的方式进行讲解，以便更加直观的观察这几个方法。

有如下类

```

class Test {
    public void test(A... a) {
        System.out.println(a);
    }
    
    
    public static void test02(String name) {
        System.out.println(name);
    }

    static class A {

    }
}    
    
```

案例一： 通过MethodHandle获取到 A这个类的对象

**步骤一：** 通过MethodHandles.lookup()获取到 MethodHandles.Lookup 对象

**步骤二：** 构造MethodType对象

**步骤三：** 通过 MethodHandles.Lookup.findConstructor(Class<?>, MethodType) 返回A类构造函数的方法句柄

**步骤四：** 调用构造函数返回A类对象

代码实现：

```

MethodHandles.Lookup lookup = MethodHandles.lookup();
MethodType methodType = MethodType.methodType(void.class);
MethodHandle constructor = lookup.findConstructor(A.class, methodType);
A o = (A) constructor.invokeExact();

```

**PS：就这么几行代码，很简单有木有**

值得注意的是构造函数是没有返回值的

案例二： 通过方法句柄执行静态方法 test02()

**步骤一：** 通过MethodHandles.lookup()获取到 MethodHandles.Lookup 对象

**步骤二：** 构造MethodType对象

**步骤三：** 通过 MethodHandles.Lookup.findStatic(Class<?>, String, MethodType) 返回test02()方法的方法句柄

**步骤四：** 调用目标方法并传递参数

代码实现：

```
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(void.class, String.class);
        MethodHandle test = lookup.findStatic(CommonUtil.class, "test02", methodType);
        test.invoke("子衿");

```

这似乎更简单

案例三： 调用实例方法test(A… a)并传递一个数组参数进去

**步骤一：** 通过MethodHandles.lookup()获取到 MethodHandles.Lookup 对象

**步骤二：** 构造MethodType对象

**步骤三：** 通过 MethodHandles.Lookup.findVirtual(Class<?>, String, MethodType) 返回test()方法的方法句柄

**步骤四：** 调用目标方法并传递参数

代码实现：

```

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(void.class, A[].class);
        MethodHandle test = lookup.findVirtual(CommonUtil.class, "test", methodType);
        test.bindTo(commonUtil).invoke(new A[]{new A()});
```

其实套路都是一样的？有木有？ 很简单的有木有？

案例四： 对name属性生成setter和getter方法句柄

**步骤一：** 通过MethodHandles.lookup()获取到 MethodHandles.Lookup 对象

**步骤二：** 通过lookup.findSetter(Class<?>, String, Class<?>) 获取setter方法的方法句柄

**步骤三：** 通过setter方法句柄对该字段重新进行赋值

**步骤四：** 通过lookup.findGetter(Class<?>, String, Class<?>) 获取getter方法的方法句柄

**步骤五：** 通过调用getter方法的方法句柄获取到当前该字段的值

代码实现：

```

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle name = lookup.findSetter(CommonUtil.class, "name", String.class);
        name.bindTo(commonUtil).invoke("改成子衿");
        MethodHandle getName = lookup.findGetter(CommonUtil.class, "name", String.class);
        System.out.println(getName.bindTo(commonUtil).invoke());

```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200719143051749.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3l1Z2UxMTIz,size_16,color_FFFFFF,t_70)

更加简单有木有！！！

### 最后的话

MethodHandle是很强大的，能做的事绝不止这么多，并且据说性能比反射更好，这篇文章只是告诉你MethodHandle简单的使用，实际上由于本人比较懒。。很多东西还没讲。。。。
# 二、[秒懂Java之方法句柄(MethodHandle)](https://blog.csdn.net/ShuSheng0007/article/details/107066856)
> 【版权申明】非商业目的注明出处可自由转载  
> 博文地址：https://blog.csdn.net/ShuSheng0007/article/details/107066856  
> 出自：shusheng007

### 文章目录

-   [概述](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_9)
-   [关键概念](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_20)
-   [如何使用](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_30)
-   -   [创建Lookup](https://blog.csdn.net/ShuSheng0007/article/details/107066856#Lookup_78)
-   [创建MethodType](https://blog.csdn.net/ShuSheng0007/article/details/107066856#MethodType_88)
-   [创建MethodHandle](https://blog.csdn.net/ShuSheng0007/article/details/107066856#MethodHandle_97)
-   [调用MethodHandle](https://blog.csdn.net/ShuSheng0007/article/details/107066856#MethodHandle_130)
-   [实际使用](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_138)
-   -   [访问构造函数](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_144)
-   [访问非private实例方法](https://blog.csdn.net/ShuSheng0007/article/details/107066856#private_156)
-   [访问private实例方法](https://blog.csdn.net/ShuSheng0007/article/details/107066856#private_165)
-   [访问非private类方法](https://blog.csdn.net/ShuSheng0007/article/details/107066856#private_174)
-   [访问非private属性](https://blog.csdn.net/ShuSheng0007/article/details/107066856#private_183)
-   [访问private属性](https://blog.csdn.net/ShuSheng0007/article/details/107066856#private_190)
-   [增强MethodHandle](https://blog.csdn.net/ShuSheng0007/article/details/107066856#MethodHandle_199)
-   [总结](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_210)
-   [抒情](https://blog.csdn.net/ShuSheng0007/article/details/107066856#_213)

相关文章：  
[秒懂Java之反射](https://blog.csdn.net/ShuSheng0007/article/details/81809999)

## 概述

众所周知，Java从最初发布时就支持反射，通过反射可以在运行时获取类型信息，但其有个缺点就是执行速度较慢。于是从Java 7开始提供了另一套API MethodHandle 。其与反射的作用类似，可以在运行时访问类型信息，但是据说其执行效率比反射更高，也被称为Java的 **现代化反射**。

官方对其定义如下：

> A method handle is a typed, directly executable reference to an underlying method, constructor, field, or similar low-level operation, with optional transformations of arguments or return values.

在《深入理解Java虚拟机》第三版中，作者也提到了MethodHandle, 但作者更多是从JVM的层面理解它，认为其主要目的是为JVM设计的一套API，以支持其他JVM语言的反射能力，例如Groovy 、Scale、Kotlin 等。

本文主要从Java编程语言的角度来看一下如何使用这套API，至于其运行效率是不是真的比反射高，以及高多少都不会涉及，有兴趣的可以自行研究。

## 关键概念

-   Lookup  
    MethodHandle 的创建工厂，通过它可以创建MethodHandle，值得注意的是检查工作是在创建时处理的，而不是在调用时处理。

-   MethodType  
    顾名思义，就是代表方法的签名。一个方法的返回值类型是什么，有几个参数，每个参数的类型什么？

-   MethodHandle  
    方法句柄，通过它我们就可以动态访问类型信息了。


## 如何使用

当理解了上面几个关键概念后使用起来就比较简单了，总的来说只需要4步：

1.  创建Lookup
2.  创建MethodType
3.  基于Lookup与MethodType获得MethodHandle
4.  调用MethodHandle

那我们接下来就按照上面4个步骤通过方法句柄来访问一下某个类里面的方法以及属性等。

首先提供一个目标类

```
public class HandleTarget {
    private String name = "hello world";

    public HandleTarget() {
    }

    public HandleTarget(String name) {
        this.name = name;
    }

    public void connectName(String name) {
        this.name = this.name + " " + name;
    }

    public String getName() {
        return name;
    }

    private void learnPrograming(String lang) {
        System.out.println(String.format("I am learning %s ", lang));
    }

    public static String declaration(String author) {
        return author + ": " + "吾生也有涯，而知也无涯。以有涯随无涯，殆己";
    }

    @Override
    public String toString() {
        return "HandleTarget{" +
                "name='" + name + '\'' +
                '}';
    }
}
```

这个类里面有两个构造函数（一个无参，一个有参），一个private **Field**, 两个public实例方法，一个public static方法以及一个private实例方法。接下来我们就具体看一下如何访问这些元素。

## 创建Lookup

使用如下代码创建一个lookup，以这种方式得到的lookup很强大，凡是调用类支持的字节码操作，它都支持。

```
 MethodHandles.Lookup lookup = MethodHandles.lookup();
```

我们还可以使用如下代码创建，但是以此种方式创建的lookup能力是受限的，其只能访问类中public的成员。

```
MethodHandles.Lookup publicLookup=MethodHandles.publicLookup();
```

## 创建MethodType

MethodType使用其静态方法创建

```
public static   MethodType methodType(Class<?> rtype, Class<?>[] ptypes)
```

第一个参数是方法的返回类型，第二参数是方法的入参

其有很多非常方便的重载，基本满足了一般的使用场景

## 创建MethodHandle

主要通过lookup里面的方法来寻找

-   创建构造函数MethodHandle

```
public MethodHandle findConstructor(Class<?> refc, MethodType type) 
```

refc: 要检索的类  
type: 对应的构造函数的MethodType

-   创建实例方法MethodHandle

```
 public MethodHandle findVirtual(Class<?> refc, String name, MethodType type)
```

name: 方法名称

-   创建类方法的MethodHandle

```
public   MethodHandle findStatic(Class<?> refc, String name, MethodType type)
```

-   创建非private的Field的访问MethodHandle 。

注意这个不是获取field的javabean Setter方法，与其毫无关系。通过这个setter 方法句柄我们就可以访问到这个属性了。

```
public MethodHandle findGetter(Class<?> refc, String name, Class<?> type)
```

对应的如果要设置此属性的值，使用Setter方法句柄

```
public MethodHandle findSetter(Class<?> refc, String name, Class<?> type)
```

## 调用MethodHandle

使用MethodHandle的invoke家族方法

```
public final native @PolymorphicSignature Object invoke(Object... args) throws Throwable;
public final native @PolymorphicSignature Object invokeExact(Object... args) throws Throwable;
...
```

## 实际使用

首先创建一个lookup

```
MethodHandles.Lookup lookup = MethodHandles.lookup();
```

## 访问构造函数

```
//无参数构造器
MethodType con1Mt = MethodType.methodType(void.class);
MethodHandle con1Mh = lookup.findConstructor(HandleTarget.class, con1Mt);
Object target1 = con1Mh.invoke();
//有参数构造器
MethodType con2Mt = MethodType.methodType(void.class, String.class);
MethodHandle con2Mh = lookup.findConstructor(HandleTarget.class, con2Mt);
Object target2 = con2Mh.invoke("ErGouWang");
```

## 访问非private实例方法

```
//调用非private实例方法
MethodType getterMt = MethodType.methodType(String.class);
MethodHandle getterMh = lookup.findVirtual(HandleTarget.class, "getName", getterMt);
String name = (String) getterMh.invoke(target2);
System.out.println(name);
```

## 访问private实例方法

```
//访问private方法
Method learnMethod = HandleTarget.class.getDeclaredMethod("learnPrograming", String.class);
learnMethod.setAccessible(true);
MethodHandle learnProMh = lookup.unreflect(learnMethod);
learnProMh.invoke(target1, "Java");
```

## 访问非private类方法

```
//调用静态方法
MethodType decMt = MethodType.methodType(String.class, String.class);
MethodHandle decMh = lookup.findStatic(HandleTarget.class, "declaration", decMt);
String dec = (String) decMh.invoke("庄子");
System.out.println(dec);
```

## 访问非private属性

```
//访问非private属性
MethodHandle nameMh= lookup.findGetter(HandleTarget.class,"name", String.class);
System.out.println((String) nameMh.invoke(con1Mh.invoke()));

```

## 访问private属性

```
//访问private的属性，需要借助反射
Field nameField = HandleTarget.class.getDeclaredField("name");
nameField.setAccessible(true);
MethodHandle nameFromRefMh = lookup.unreflectGetter(nameField);
System.out.println((String) nameFromRefMh.invoke(target1));

```

## 增强MethodHandle

```
//增强MethodHandle
MethodType setterMt = MethodType.methodType(void.class, String.class);
MethodHandle setterMh = lookup.findVirtual(HandleTarget.class, "connectName", setterMt);
MethodHandle bindedSetterMh = setterMh.bindTo(target2);
bindedSetterMh.invoke("love CuiHuaNiu");
System.out.println((String) getterMh.invoke(target2));
```

当我们创建了"connectName"方法的MethodHandle，可以不立即调用而是将其绑定到某个对象上，这个对象的类型必须是`HandleTarget`及其子类，那么调用重新获得MethodHandle时，其会调用到新绑定对象里面的那个方法上。

## 总结

江湖流传着一种说法：使用MethodHandle就像是在用Java来写字节码。这种说法是有一定道理的，因为MethodHandle里的很多操作都对应着相应的字节码。总的来说，其与反射一样，离应用型程序员日常开发比较远，但是在开发框架和和工具包时却会被大量使用。

## 抒情

> 人生在世不称意，明朝散发弄扁舟

本文源码地址：[ToMasterJava](https://github.com/shusheng007/ToMasterJava)

# 三、[java方法句柄-----1.方法句柄类型、调用](https://www.cnblogs.com/tangliMeiMei/p/12983627.html#1%E6%96%B9%E6%B3%95%E5%8F%A5%E6%9F%84%E7%9A%84%E7%B1%BB%E5%9E%8B)