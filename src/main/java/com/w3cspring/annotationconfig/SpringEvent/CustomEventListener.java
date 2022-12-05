package com.w3cspring.annotationconfig.SpringEvent;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;


@Component
public class CustomEventListener<T extends ApplicationEvent> implements ApplicationListener<T>, Serializable {
    @Override
    public void onApplicationEvent(T event) {
        System.out.println("CustomEventListener"+event.toString());
    }
}
