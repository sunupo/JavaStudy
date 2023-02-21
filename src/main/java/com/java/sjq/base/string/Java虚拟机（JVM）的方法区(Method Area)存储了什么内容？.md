[https://www.breakyizhan.com/javamianshiti/2839.html](https://www.breakyizhan.com/javamianshiti/2839.html)

# 1、[JVM内存模型](https://so.csdn.net/so/search?q=JVM%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B&spm=1001.2101.3001.7020)

大多数[JVM](https://www.breakyizhan.com/javamianshiti/2830.html)将内存分配为Method Area(方法区)、Heap(堆)、Program Counter Register(程序计数器)、JAVA Method Stack(JAVA方法栈)、[Native](https://so.csdn.net/so/search?q=Native&spm=1001.2101.3001.7020) Method Stack(本地方法栈)。

# 2、 方法区（Method Area）

线程共享，存储已经被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等等。（HotSpot虚拟机上开发部署人员更愿意成为“永久代”，Permanent Generation）。示意图如下，下面的图片显示的是JVM加载类的时候，方法区存储的信息：

![](https://img-blog.csdnimg.cn/20200827093745562.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTQ2ODM0ODg=,size_16,color_FFFFFF,t_70)

![](https://img-blog.csdnimg.cn/2020082709404662.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTQ2ODM0ODg=,size_16,color_FFFFFF,t_70)

### 1、类型信息

-   类型的全限定名
-   超类的全限定名
-   直接超接口的全限定名
-   类型标志（该类是类类型还是接口类型）
-   类的访问描述符（public、private、default、abstract、final、static）

### 2、类型的常量池

存放该类型所用到的常量的有序集合，包括直接常量（如字符串、整数、浮点数的常量）和对其他类型、字段、方法的符号引用。常量池中每一个保存的常量都有一个索引，就像数组中的字段一样。因为常量池中保存中所有类型使用到的类型、字段、方法的字符引用，所以它也是动态连接的主要对象（在动态链接中起到核心作用）。

### 3、字段信息（该类声明的所有字段）

-   字段修饰符（public、protect、private、default）
-   字段的类型
-   字段名称

### 4、方法信息

方法信息中包含类的所有方法，每个方法包含以下信息：

-   方法修饰符
-   方法返回类型
-   方法名
-   方法参数个数、类型、顺序等
-   方法字节码
-   操作数栈和该方法在栈帧中的局部变量区大小
-   异常表

### 5、类变量（静态变量）

指该类所有对象共享的变量，即使没有任何实例对象时，也可以访问的类变量。它们与类进行绑定。

### 6、  指向类加载器的引用

每一个被JVM加载的类型，都保存这个类加载器的引用，类加载器动态链接时会用到。

### 7、指向Class实例的引用

类加载的过程中，虚拟机会创建该类型的Class实例，方法区中必须保存对该对象的引用。通过Class.forName(String className)来查找获得该实例的引用，然后创建该类的对象。

### 8、方法表

为了提高访问效率，JVM可能会对每个装载的非抽象类，都创建一个数组，数组的每个元素是实例可能调用的方法的直接引用，包括父类中继承过来的方法。这个表在抽象类或者接口中是没有的，类似C++虚函数表vtbl。

### 9、运行时常量池(Runtime Constant Pool)

Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是常量池，用于存放编译器生成的各种字面常量和符号引用，这部分内容被类加载后进入方法区的运行时常量池中存放。

运行时常量池相对于Class文件常量池的另外一个特征具有动态性，可以在运行期间将新的常量放入池中（典型的如String类的intern()方法）。

（这个地方不太理解，网上找来的解释不知道对否：运行时常量池是把Class文件常量池加载进来，每个类有一个独立的。刚开始时运行的时候常量池里的链接都是符号链接（只用名字没有实体），跟在Class文件里的一样；边运行边把常量转换成直接链接。例如说要Class A调用Foo.bar()方法，A.class文件里就会有对该方法的Method ref常量，是个符号链接（只有名字没有实体），加载到运行时常量池也还是一样是符号链接，等真的要调用该方法的时候该常量就会被resolve为一个直接链接（直接指向要调用的方法的实体））。