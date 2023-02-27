相同 bean name，注入bean优先级

- xml > student.class(@Component) > SpringConfiguration.class(@Configuration)
- xml 不允许定义 id 相同的 beanId
- SpringConfiguration.class 相同的bean name，那么按照代码顺序先定义的生效
- 
```java
// 上面的studentC生效，下面的不会运行
@Bean(name = "studentC")
public Student setStudentC(){
return new Student(3,"studentC");
}
@Bean(name = "studentC")
public Student setStudentD(){
return new Student(4,"studentD");
}
```


[XML 中的 xmlns、xmlns:xsi、xsi:schemaLocation](https://blog.csdn.net/qq_40395874/article/details/114280229)