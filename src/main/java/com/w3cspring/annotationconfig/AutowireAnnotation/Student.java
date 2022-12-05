package com.w3cspring.annotationconfig.AutowireAnnotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//@Component("studentAutowire")
@Component("student")
@Scope(value = "prototype")
public class Student {
//    @Value("18")
    private Integer age;
//    @Value("sunjingqin")
    private String name;

    public void setAge(Integer age) {
        this.age = age;
    }
    public Integer getAge() {
        return age;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Autowired
    public Student(@Value("0") Integer age, @Value("default name") String name) {
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}