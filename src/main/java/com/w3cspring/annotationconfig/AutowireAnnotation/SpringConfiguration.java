package com.w3cspring.annotationconfig.AutowireAnnotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfiguration {
    @Bean(name = "student")
    public Student setStudentA(){
        return new Student(1,"studentA");
    }
    @Bean(name = "student")
    @Primary
    public Student setStudentB(){
        return new Student(2,"studentB");
    }
    @Bean(name = "studentC")
    public Student setStudentC(){
        return new Student(3,"studentC");
    }
    @Bean(name = "studentC")
    public Student setStudentD(){
        return new Student(4,"studentD");
    }
    @Bean(name = "teacherConfig")
    public Teacher setTeacher(){
        return new Teacher(4,"configuration teacher");
    }
}
