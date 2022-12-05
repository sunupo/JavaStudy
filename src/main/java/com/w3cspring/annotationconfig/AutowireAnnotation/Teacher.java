package com.w3cspring.annotationconfig.AutowireAnnotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("teacher")
@Scope(value = "prototype")
public class Teacher {
    private Integer age;
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
    public Teacher(@Value("0") Integer age, @Value("default teacher name") String name) {
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}