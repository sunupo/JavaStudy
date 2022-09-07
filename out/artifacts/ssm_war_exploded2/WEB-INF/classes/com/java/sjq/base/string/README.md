> 这一篇用反编译解释了[字符串常量池StringTable简介](https://blog.csdn.net/john2333/article/details/120762152)
- 字面量创建字符串
- 字符串变量拼接
- 字符串常量拼接
- intern方法

> 转自 [关于java的String一些问题（创建过程、intern()方法、字符串常量池），jdk6/jdk7/jdk8，详细到底层源码和jvm](https://blog.csdn.net/Xiaofeng_Lu__/article/details/123459899)
# 0. 先验知识
①要先对java虚拟机、String和StringBuilder源码有一定了解
②String的intern方法：这是一个native方法，调用的是本地底层c++的方法，作用为：

jdk<7：在常量池中查找，如果有，则返回常量池中该字符串字面量的引用地址，没有则在池中新建字符串字面量，然后返回该字面量的引用地址。
jdk>=7：则是将字符串字面量本身在池中查找，如果有，则返回池中的引用地址，这里没有变化，如果没有则会将堆中的引用传递给常量池，然后返回这个指向堆的引用。也就是说，jdk>=7堆中不会创建两个相同的字面量字符串，最多只会在常量池中增加一个引用地址的值，实质上没有产生新东西，这样可以减少字符串的创建。
③“引用”、“地址”、“引用地址”这三个词都是同一个意思。
④本文中“常量池”默认为字符串常量池
![img.png](img.png)

# 1. 先看图：jdk6，7，8的jvm结构图
- jdk6永久代使用的是虚拟机内存，字符串常量池(String Table)在方法区中:

![img_1.png](img_1.png)

- jdk7永久代使用的是虚拟机内存，不过字符串常量池(String Table)移到了堆中:
![img_2.png](img_2.png)

- jdk8方法区用元空间来实现，不使用虚拟机内存，使用的是本地内存，字符串常量池(String Table)仍然保留在堆中:
![img_3.png](img_3.png)
# 2 创建String对象的几种方式以及其底层过程
(由于jdk7和jdk8的字符串常量池都是在堆中，所以以下几种方式中jdk7等同jdk8)
耐心去理解领悟下面7个例子，就能明白所有关于String的底层问题！
## 2.1 以字面量的方式创建的字符串"abc"直接放到常量池中
### **示例**：
```
String s1 = "abc";
String s2 = "abc";
System.out.println(s1 == s2); //true
```
### **内存分析**

对于s1这行代码，jvm执行引擎会以字面量的方式创建的字符串"abc"直接放到常量池中（jdk6/7都是），s1作为变量存在栈中，然后把池中"abc"的引用地址赋值给变量s1。
对于s2这行代码，本该创建的字符串"abc"直接放到常量池中，发现池中已经有"abc",于是直接把"abc"的引用地址赋值给变量s2。
也就是，s1和s2保存的都是池中"abc"的地址，所以s1 == s2为true

### **图例**

![img_4.png](img_4.png)

## 2.2 以字面量的方式创建的字符串"abc"直接放到常量池中
### **示例**：
```
String s1 = "abc"+"def";
String s2 = "abcdef";
System.out.println(s1 == s2); //true（jdk7）
```
### **内存分析**

(jdk7中，在jdk6中也差不多，只是常量池在jvm中的位置不一样)

- 对于s1这行代码，“abc"和"def"不会放入常量池，编译器在编译时产生的字节码已经将 “abc” + “def” 优化成了 "abcdef"
放入常量池，然后返回池中"abcdef"的引用地址赋值给变量s1。具体可以看反编译字节码指令，不会出现"abc"和"def”:
![img_5.png](img_5.png)

- 对于s2这行代码，"abcdef"本应该放入常量池，可是由于已存在相同的字符串，于是返回原本存在的"abcdef"的引用赋值给变量s2

### **图例**

![img_6.png](img_6.png)



## 2.3 new String("abc");
### **示例**：
```
String s1 = new String("abc");
String s2 = s1.intern();
System.out.println(s1 == s2); //false

```
### **内存分析**
**① 创建过程 new String("abc")**（**jdk7同jdk6，只是jdk7的常量池在堆中。**）
- 先在常量池中创建了"abc"这个字面量，然后在堆中创建了一个String对象
- **s1 的属性 value** 指向的是***字符串常量池***中的 "abc"，**变量 s1** 指向的是***堆***中的这个String对象。

**why？为什么会先在常量池中创建了"abc"这个字面量**？
看new String(“abc”)这个构造器方法的源码:
"abc"字面量作为实参传给构造器的形参，这个形参可以看作为方法的变量，给形参赋值的过程相当于：String original = “abc”,于是就把"abc"当做字面量放进了常量池
```
    public String(String original) { //构造器源码
        this.value = original.value;
        this.hash = original.hash; 
    }

```

**② intern()**

s1.intern()返回的是常量池的"abc"的引用地址,所以s2的值为池中"abc"的引用地址，s1的值为堆中（非池中）对象的地址，所以s1 == s2 返回false
所以jdk6和7中过程一样，只是常量池位置不一样

### **图例**
- jdk 6↓ ![img_7.png](img_7.png)
- jdk 7↓ ![img_8.png](img_8.png)

## 2.2 以字面量的方式创建的字符串"abc"直接放到常量池中
### **示例**：
```

```
### **内存分析**


### **图例**
