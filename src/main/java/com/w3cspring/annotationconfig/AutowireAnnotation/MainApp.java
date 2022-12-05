package com.w3cspring.annotationconfig.AutowireAnnotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/spring-Beans.xml");
        Student student = (Student) context.getBean("student");
        System.out.println("Name : " + student.getName());
        System.out.println("Age : " + student.getAge());
        StudentClass studentClass = (StudentClass) context.getBean("studentClass");
        System.out.println("studentClass Name : " + studentClass.getStudent().getName());
        System.out.println("studentClass Age : " + studentClass.getStudent().getAge());
        System.out.println("studentClass teacher Name : " + studentClass.getTeacher().getName());
        System.out.println("studentClass teacher Age : " + studentClass.getTeacher().getAge());

        final String[] beanDefinitionNames = context.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).forEach(System.out::println);

    }
}