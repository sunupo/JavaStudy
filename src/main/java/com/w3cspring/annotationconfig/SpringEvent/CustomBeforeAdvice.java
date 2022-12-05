package com.w3cspring.annotationconfig.SpringEvent;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;


public class CustomBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] objects, Object o) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Arrays.stream(parameterTypes).forEach(System.out::println);
        System.out.println("before methodBeforeAdvice");
    }
}
