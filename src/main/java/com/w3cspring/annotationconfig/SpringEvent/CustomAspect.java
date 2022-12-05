package com.w3cspring.annotationconfig.SpringEvent;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;


public class CustomAspect {
    public void around() throws Throwable {
        System.out.println("CustomAspect around");
    }
}
