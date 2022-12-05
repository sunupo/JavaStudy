package com.w3cspring.annotationconfig.SpringEvent;

import org.springframework.stereotype.Component;

@Component
public class SubEventListenerGeneric<T extends CustomEvent> extends CustomEventListener<T> {


    @Override
    public void onApplicationEvent(T event) {
        System.out.println("SubEventListenerGeneric" + event.toString());
    }
}
