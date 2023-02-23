**对于子类调用父类的方法我们用super.Method()即可，但是倘若我们想调用其祖先类，并且在不改变其继承关系以及祖先和父类的代码的时候，该怎么办呢，代码样列如下：给大家三个类，可以先去了解再来熟悉下面的代码**

-   MethodHandle 它是可对直接执行的方法或者字段或者构造方法的类型的引用，或者说他是一个有能力安全调用方法的对象。
-   MethodHandles 它是仅操作或返回方法句柄的静态方法的类。
-   MethodType 他是表示方法签名类型的不可变对象。每个MethodHandle都有一个MethodType实例，用来指明返回类型和参数类型。

```
package vip.wulang.test;

/**
 * @author coolerwu on 2018/8/13.
 * @version 1.0
 * @time 20:58
 */
public class ParentTest {
    class GrandFather {
        public void thinking() {
            System.out.println("i am grandfather.");
        }
    }

    class Father extends GrandFather {
        @Override
        public void thinking() {
            System.out.println("i am father.");
        }
    }

    class Son extends Father {
        @Override
        public void thinking() {
            //如何调用GrandFather
        }
    }

    public static void main(String[] args) {
        new ParentTest().new Son().thinking();
    }
}

```

**第一种方法**

```
package vip.wulang.test;

/**
 * @author coolerwu on 2018/8/13.
 * @version 1.0
 * @time 20:58
 */
public class ParentTest {
    class GrandFather {
        public void thinking() {
            System.out.println("i am grandfather.");
        }
    }

    class Father extends GrandFather {
        @Override
        public void thinking() {
            System.out.println("i am father.");
        }
    }

    class Son extends Father {
        @Override
        public void thinking() {
            GrandFather grandFather = new GrandFather();
            grandFather.thinking();
        }
    }

    public static void main(String[] args) {
        new ParentTest().new Son().thinking();
    }
}
//输出结果
//i am grandfather.
```

**第二种方法也就是今天我要讲的这种，其他的反射之类的各位自行去解决**

```
package vip.wulang.test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

/**
 * @author coolerwu on 2018/8/13.
 * @version 1.0
 * @time 20:58
 */
public class ParentTest {
    class GrandFather {
        public void thinking() {
            System.out.println("i am grandfather.");
        }
    }

    class Father extends GrandFather {
        @Override
        public void thinking() {
            System.out.println("i am father.");
        }
    }

    class Son extends Father {
        @Override
        public void thinking() {
            MethodType methodType = MethodType.methodType(void.class);
            try {
                Constructor<MethodHandles.Lookup> constructor = 
                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                MethodHandles.Lookup instance = constructor.newInstance(GrandFather.class, -1);
                MethodHandle methodHandle = 
                instance.findSpecial(
                GrandFather.class, "thinking", methodType, GrandFather.class);
                methodHandle.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ParentTest().new Son().thinking();
    }
}
//输出结果
//i am grandfather.
```

**是不是很神奇**

```
其中MethodType.methodType();第一个参数是返回类型,后面的为参数类型。MethodHandles.Lookup中的构造方法我需要
的是private Lookup(Class<?> lookupClass, int allowedModes)所以我利用了反射来获取到实例，也可以通过反射
获取这个static final Lookup IMPL_LOOKUP = new Lookup(Object.class, TRUSTED);都是一样的，主要是因为在
Lookup类中TRUSTED代表可信任的，可以访问任何方法，TRUSTED值为-1，而instance.findSpecial意思就是去这个类寻
找带有thinking名字的并且参数类型以及方法类型一样的，最后invoke
```

**这种方法模式使用了invokedynamic指令，使其具有动态语言的特性，用空可以去看看《深入理解Java虚拟机》这本书，可以参悟很多！！！**