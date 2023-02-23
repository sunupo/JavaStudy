[(116条消息) java基础之反射类型Type\_java 反射 类型\_Gerald Newton的博客-CSDN博客](https://blog.csdn.net/m0_68064743/article/details/123957060)
> [JAVA中parameterized,谈谈Java类型中ParameterizedType，GenericArrayType，TypeVariabl，WildcardType...\_元宿six的博客-CSDN博客](https://blog.csdn.net/weixin_31236101/article/details/116045926?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-4-116045926-blog-102940035.pc_relevant_3mothn_strategy_and_data_recovery&spm=1001.2101.3001.4242.3&utm_relevant_index=7)

Java在加入泛型之后，仅仅Class已经不足以描述数据的类型了，比如List类型的数据，其Class的类型为List.class，但是其类型包含了泛型参数，所以java引入了Type类型来描述泛型类型。除了泛型类型，还有数组格式的数据，其类型也包含两部分，一部分是数组对象本身的class，另外一部分是数组中数据的类型。本文会详细介绍JavaType中的各种类型，分析这些类型的使用方法。

## Type介绍

Type是Java 编程语言中所有类型的公共高级接口，也就是Java中所有"类型"的接口。官方原话定义如下

> Type is the common superinterface for all types in the Java programming language. These include raw types, parameterized types, array types, type variables and primitive types.

这样的官方描述有点难懂，此处我画个图解释一下。Type其实是和泛型一起出现的，可以说Type就是为了支持泛型。

-   泛型出现之前，我们可以通过Class来确认一个对象的类型，比如ClassA A，那么A的类型就是ClassA;
-   泛型出现之后，显然不能通过Class唯一确认一个对象的类型，比如List A，A的Class是List，但是A的类型显然不仅仅是List，它是由Class类型的List + TypeVariables的ClassA联合确认的一个Type。

![[图片上传失败...(image-f4135a-1649064645695)]](https://img-blog.csdnimg.cn/cd88082cb3034b92a98f5e10dc853657.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

> A type variable is an unqualified identifier used as a type in class, interface, method, and constructor bodies.

## Type的类型

Type可以分为两大类：包含TypeVariables和不包含TypeVariables的类型：

-   **不包含TypeVariable**：包含基本数据类型（int, long等），基本Class（如Object，不包含泛型的类）；

-   **包含TypeVariable**，按照包含的TypeVariable又分为以下几类：

    -   ParameterizedType: 表示一种参数化的类型，如List，泛型的参数已经指定；
    -   GenericArrayType: 表示一种元素类型是参数化类型或者类型变量的数组类型，如List\[\]\[\]；
    -   WildcardType: 代表一种通配符类型表达式，比如List<?>, List<? extends ClassA>, List<? super Object>。

![[图片上传失败...(image-b5b130-1649064645695)]](https://img-blog.csdnimg.cn/c856ad198ee049ea9c7c7db70b4052ac.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

继续介绍Type之前，需要先介绍一下java的泛型机制：

泛型是Java SE 1.5的新特性，泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数。这种参数类型可以用在类、接口和方法的创建中，分别称为泛型类、泛型接口、泛型方法。 Java语言引入泛型的好处是安全简单。泛型的好处是在编译的时候检查类型安全，并且所有的强制转换都是自动和隐式的，以提高代码的重用率。

泛型信息只存在于代码编译阶段，在进入 JVM 之前，与泛型相关的信息会被擦除掉，专业术语叫做类型擦除。

## 参数化类型ParameterizedType

参数化类型的写法如下：C<T1,…,Tn>，其中C是Class类型，<T1,…,Tn> 是Type，先列几个参数化类型的合法定义：

```
Seq\<String\>
Seq\<Seq\<String\>\>
Seq\<String\>.Zipper\<Integer\>
Pair\<String,Integer\>

```

ParameterizedType类型的接口方法介绍：

| 返回值 | 方法名称 | 描述信息 |
| --- | --- | --- |
| Type\[\] | getActualTypeArguments() | 参数化类型中的TypeVariable参数类型，如List 返回 String.class, List<List<> 返回List< |
| Type | getOwnerType() | 获取当前Type所属的Type，比如对于O.I~中的I~类型，会返回 O~~ |
| Type | getRawType() | 获取当前Type的Class，如List 返回 List.class |

![[图片上传失败...(image-8f99de-1649064645695)]](https://img-blog.csdnimg.cn/fcb406c073ac44d3835b3e1f86dedad4.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

> ParameterizedType represents a parameterized type such as Collection.A parameterized type is created the first time it is needed by a reflective method, as specified in this package. When a parameterized type p is created, the generic type declaration that p instantiates is resolved, and all type arguments of p are created recursively. See TypeVariable for details on the creation process for type variables. Repeated creation of a parameterized type has no effect. Instances of classes that implement this interface must implement an equals() method that equates any two instances that share the same generic type declaration and have equal type parameters.

## 数组类型GenericArrayType

数组泛型类型的写法如下：C\[\]，其中C是Class类型， 是Type，先列几个数组泛型类型的合法定义：

```
List\<String\>[]
List\<Seq\<String\>\> [][]

```

| 返回值 | 方法名称 | 描述信息 |
| --- | --- | --- |
| Type | getGenericComponentType() | 数组元素的类型，如List \[\]返回List |

![[图片上传失败...(image-978cf6-1649064645695)]](https://img-blog.csdnimg.cn/0a39fdff2b5c4c15b2a5349d0440c4da.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

**注意：**<>不能出现在数组的初始化中，即new数组之后不能出现<>，否则javac无法通过。但是作为引用变量或者方法的某个参数是完全可以的。不包含泛型的数组本节不做介绍(如String\[\])，下文中会进行介绍。

> GenericArrayType represents an array type whose component type is either a parameterized type or a type variable.

## 通配符类型WildcardType

通配符类型何其字面意思相同，其泛型类型不再是一个具体的类，而是一个通配符表达式，表达式包含以下三种：“?”,“? extends Type”, “? super Type”，其中Type可以为WildcardType，GenericArrayType，ParameterizedType，Class.

> WildcardType represents a wildcard type expression, such as ?, ? extends Number, or ? super Integer.

WildcardType 接口的方法和介绍如下.

| 返回值 | 方法名称 | 描述信息 |
| --- | --- | --- |
| Type\[\] | getLowerBounds() | 返回通配Type的下限类型，现阶段返回值的长度为1 |
| Type\[\] | getUpperBounds() | 返回通配Type的上限类型，现阶段返回值的长度为1 |

![[图片上传失败...(image-77032e-1649064645694)]](https://img-blog.csdnimg.cn/3e58d9ba93d24ee685cc9a7a9d4bc73d.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

## 基本Class、基本数据类型和数组

通过反射获取基本的Class和基本数据类型此处就不详细介绍了，接下来会重点介绍一下数组类型。java的数组类型由虚拟机生成，虚拟机生成的数组类型的名称一般类似于"class \[\[Ljava.lang.String;“，注意其中的”\[\["表示是二维数组。那么如何获取数组中的元素类型呢？ java.lang.Class包中提供了以下接口查询：

| 返回值 | 方法名称 | 描述信息 |
| --- | --- | --- |
| Class<?> | componentType() | 如果类型是数组类型，返回数组中元素的类型，否则返回null |

> componentType():Returns the component type of this Class, if it describes an array type, or null otherwise.

![[图片上传失败...(image-89a590-1649064645694)]](https://img-blog.csdnimg.cn/2b51435a60c04df3ace8ec7548c9d4c0.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

## 如何获取字段或参数的Type信息

平时使用java程序的过程中，我们接触到的最多的类型只有Class，像泛型类型和数组类型，通常只有通过反射才能获取到。

## 获取字段的泛型信息

如下程序中，我们首先定义了一个自定义的类TestParameterizedType，只包含一个字段List field，然后我们在另外一个单测实例中尝试通过反射获取field的相关信息。通过field.getType()我们获取到了field的类型。通过field.getGenericType()我们获取到了field的泛型信息。

```
public class ReflectParameterizedTypeTest {

    public static class TestParameterizedType {
        private List\<String\> field;
    }

    @Test
    public void testIntType() throws NoSuchFieldException {
        Class\<?\> clazz = TestParameterizedType.class;
        Field field = clazz.getDeclaredField("field");

        // 此处获取到字段的实际Class类型
        Class\<?\> clazzType = field.getType();
        System.out.println("Field type: " + clazzType.getName());

        // 此处获取到字段的泛型类型
        Type genericType = field.getGenericType();
        System.out.println("Field generic type: " + field.getGenericType().getTypeName());
    }
}

```

![[图片上传失败...(image-26b75-1649064645694)]](https://img-blog.csdnimg.cn/177c05f3f5cf498f9605db38eb5a6dbe.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAR2VyYWxkIE5ld3Rvbg==,size_20,color_FFFFFF,t_70,g_se,x_16)

## 获取方法参数的泛型信息

类似于字段的获取方式，方法可以通过`Method.getGenericParameterTypes()`获取所有参数的泛型信息。

## 获取运行时变量的泛型信息

不可能，具体原因参考java的泛型擦除原理。

> **在此我向大家推荐一个架构学习交流圈。交流学习伪鑫：539413949（里面有大量的面试题及答案）里面会分享一些资深架构师录制的视频录像：有Spring，MyBatis，Netty源码分析，高并发、高性能、分布式、微服务架构的原理，JVM性能优化、分布式架构等这些成为架构师必备的知识体系。还能领取免费的学习资源，目前受益良多**