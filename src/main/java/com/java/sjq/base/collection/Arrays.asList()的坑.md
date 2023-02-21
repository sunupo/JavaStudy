
**【1. 要点】**

该方法是将数组转化成List集合的方法。

List<String> list = Arrays.asList("a","b","c");

注意：

（1）该方法适用于对象型数据的数组（String、Integer...）

（2）该方法不建议使用于基本数据类型的数组（byte,short,int,long,float,double,boolean）

（3）**该方法将数组与List列表链接起来：当更新其一个时，另一个自动更新**

（4）不支持add()、remove()、clear()等方法

**【2.Arrays.asList()是个坑】**

用此方法得到的List的长度是不可改变的，

当你向这个List添加或删除一个元素时（例如 list.add("d");）程序就会抛出异常（java.lang.UnsupportedOperationException）。 怎么会这样？只需要看看asList()方法是怎么实现的就行了：

public static <T> List<T> asList(T... a) {return new ArrayList<>(a);}

当你看到这段代码时可能觉得没啥问题啊，不就是返回了一个ArrayList对象吗？问题就出在这里。

这个ArrayList不是java.util包下的，而是java.util.Arrays.ArrayList

它是Arrays类自己定义的一个静态内部类，这个内部类没有实现add()、remove()方法，而是直接使用它的父类AbstractList的相应方法。

而AbstractList中的add()和remove()是直接抛出java.lang.UnsupportedOperationException异常的！

public void add(int index, E element) { throw new UnsupportedOperationException();}

public E remove(int index) {throw new UnsupportedOperationException();}

**总结：如果你的List只是用来遍历，就用Arrays.asList()。**

**如果你的List还要添加或删除元素，还是乖乖地new一个java.util.ArrayList，然后一个一个的添加元素。**

**【3.示例代码】**

```java
package cn.wyc; import java.util.Arrays;import java.util.List; public class Test {    public static void main(String[] args){        //1、对象类型(String型)的数组数组使用asList()，正常        String[] strings = {"aa", "bb", "cc"};        List<String> stringList = Arrays.asList(strings);        System.out.print("1、String类型数组使用asList()，正常：  ");        for(String str : stringList){            System.out.print(str + " ");        }        System.out.println();          //2、对象类型(Integer)的数组使用asList()，正常        Integer[] integers = new Integer[] {1, 2, 3};        List<Integer> integerList = Arrays.asList(integers);        System.out.print("2、对象类型的数组使用asList()，正常：  ");        for(int i : integerList){            System.out.print(i + " ");        }//        for(Object o : integerList){//            System.out.print(o + " ");//        }        System.out.println();          //3、基本数据类型的数组使用asList()，出错        int[] ints = new int[]{1, 2, 3};        List intList = Arrays.asList(ints);        System.out.print("3、基本数据类型的数组使用asList()，出错(输出的是一个引用，把ints当成一个元素了)：");        for(Object o : intList){            System.out.print(o.toString());        }        System.out.println();         System.out.print("   " + "这样遍历才能正确输出：");        int[] ints1 = (int[]) intList.get(0);        for(int i : ints1){            System.out.print(i + " ");        }        System.out.println();         //4、当更新数组或者List,另一个将自动获得更新        System.out.print("4、当更新数组或者List,另一个将自动获得更新：  ");        integerList.set(0, 5);        for(Object o : integerList){            System.out.print(o + " ");        }        for(Object o : integers){            System.out.print (o + " ");        }        System.out.println();         //5、add()   remove() 报错        System.out.print("5、add()   remove() 报错：  ");//        integerList.remove(0);//        integerList.add(3, 4);//        integerList.clear();     } }
```

输出：

```java
1、String类型数组使用asList()，正常：  aa bb cc 2、对象类型的数组使用asList()，正常：  1 2 3 3、基本数据类型的数组使用asList()，出错(输出的是一个引用，把ints当成一个元素了)：[I@1540e19d   这样遍历才能正确输出：1 2 3 4、当更新数组或者List,另一个将自动获得更新：  5 2 3 5 2 3 5、add()、remove()、clear() 报错： 
```