package com.w3cspring.annotationconfig.SpringEvent;

import org.springframework.context.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {

    private final Integer id;

    @Override
    public String toString() {
        return "CustomEvent{" +
                "id=" + id +
                ", source=" + source +
                '}';
    }

    public Integer getId() {
        return id;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public CustomEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }
}
