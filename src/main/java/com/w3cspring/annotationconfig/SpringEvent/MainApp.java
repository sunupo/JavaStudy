package com.w3cspring.annotationconfig.SpringEvent;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

public class MainApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring/spring-Beans.xml");
        Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);

        context.registerShutdownHook();

        CustomEventPublisher publisher = (CustomEventPublisher) context.getBean("customEventPublisher");
        publisher.customPublish(new CustomEvent(context, 98));

        final CustomEventListener customEventListener = new CustomEventListener();
//        final SubEventListenerGeneric customEventListener = new SubEventListenerGeneric();
        Type[] genericInterfaces = customEventListener.getClass().getGenericInterfaces();
//        https://blog.csdn.net/m0_68064743/article/details/123957060 java基础之反射类型Type
        Arrays.stream(genericInterfaces).forEach((item) -> {
            if(item instanceof ParameterizedType){
                System.out.println("ParameterizedType---------------------:\t"+item.getTypeName());
                System.out.println(((ParameterizedType) item).getClass());
                System.out.println(((ParameterizedType) item).getOwnerType());
                try {
                    Class<?> clz = Class.forName(((ParameterizedType) item).getRawType().getTypeName());
                    System.out.println(clz.isInterface()+""+clz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println(((ParameterizedType) item).getRawType()+"--"+
                        ((ParameterizedType) item).getRawType().getClass().isInterface());
                Arrays.stream(((ParameterizedType) item).getActualTypeArguments()).forEach(System.out::println);
            }else
                System.out.println("genericInterfaces:\t"+item.getTypeName());

        });


        Type genericSuperclass = customEventListener.getClass().getGenericSuperclass();
        System.out.println("genericSuperclass"+genericSuperclass.getTypeName());
        context.start();
//        context.refresh();
//        context.stop();
//        context.close();
    }
}
