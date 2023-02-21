### 文章目录

-   [前言](#_5)
-   [一、什么是方法引用？](#_10)
-   [二、方法引用的几种类型](#_13)
-   -   [1.静态方法引用（类名::静态方法名）](#1_14)
-   [2.实例方法引用（对象名::实例方法名）](#2_46)
-   [3.类的实例方法引用（类名::实例方法名）](#3_84)
-   [总结](#_118)

* * *

# 前言

这篇文章主要介绍什么是[方法引用](https://so.csdn.net/so/search?q=%E6%96%B9%E6%B3%95%E5%BC%95%E7%94%A8&spm=1001.2101.3001.7020)，以及方法引用的一些使用规则。（文章只是我个人的理解，如有问题欢迎指出）

* * *

# 一、什么是方法引用？

方法引用时jdk1.8之后引入的一种[语法糖](https://so.csdn.net/so/search?q=%E8%AF%AD%E6%B3%95%E7%B3%96&spm=1001.2101.3001.7020)操作。作用是简化lambada在调用已经存在的方法时的表达式。

# 二、方法引用的几种类型

## 1.静态方法引用（类名::静态方法名）

代码如下：

```c
public class Test {
    public static void main(String[] args) {
    
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student(10));
        
        // 类名::静态方法名 
        //注意事项：
        // 1.compare方法是静态的
        // 2.参数列表与 java.util.Comparator.compare(T o1, T o2) 参数列表保持一致
        students.sort(Student::compare);
    }

    static class Student {
        int age;
        public Student(int age) {
            this.age = age;
        }
        public int getAge() {
            return age;
        }
		//静态方法
        public static int compare(Student student1, Student student2) {
            return student1.getAge() - student2.getAge();
        }
    }
}

12345678910111213141516171819202122232425262728
```

## 2.[实例方法](https://so.csdn.net/so/search?q=%E5%AE%9E%E4%BE%8B%E6%96%B9%E6%B3%95&spm=1001.2101.3001.7020)引用（对象名::实例方法名）

代码如下：

```c
public class Test {
    public static void main(String[] args) {

        ArrayList<Student> students = new ArrayList<>();
        Student student = new Student(10);
        students.add(student);

        // 对象名::实例方法名
        //注意事项：
        // 1.compare方法是非静态的，方法由对象调用
        // 2.参数列表与 java.util.Comparator.compare(T o1, T o2) 参数列表保持一致
        students.sort(student::compare);


    }

    static class Student {
        int age;

        public Student(int age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        //对象方法
        public int compare(Student student1, Student student2) {
            return student1.getAge() - student2.getAge();
        }
    }
}

12345678910111213141516171819202122232425262728293031323334
```

## 3.类的实例方法引用（类名::实例方法名）

代码如下：

```c
 public static void main(String[] args) {

        ArrayList<Student> students = new ArrayList<>();
        Student student = new Student(10);
        students.add(student);

        // 类名::实例方法名
        //注意事项：
        // 1.compare方法是非静态的
        // 2.java.util.Comparator.compare(T o1, T o2) 参数列表中的第一个参数
        // 在compare方法中被省略（参数 o1 默认为调用者本身）
        students.sort(Student::compare)

    }

    static class Student {
        int age;
        public Student(int age) {
            this.age = age;
        }
        public int getAge() {
            return age;
        }
		//相比于java.util.Comparator.compare(T o1, T o2)接口参数，少了一个参数
        public int compare(Student student) {
            return this.getAge() - student.getAge();
        }
    }
12345678910111213141516171819202122232425262728
```

# 总结

以上就是方法调用的使用。主要分为：静态方法引用，实例方法引用，类的实例方法引用。