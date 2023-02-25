package com.soecode.lyf.AttributeListener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * 图书实体
 */
@WebListener
public class SSMAttributeListener implements HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {

        System.out.println("HttpSessionAttributeListener\tattributeAdded:\t" + event.getName()+ event.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        System.out.println("HttpSessionAttributeListener\tattributeRemoved:\t" + event.getName()+ event.getValue());

    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        System.out.println("HttpSessionAttributeListener\tattributeReplaced:\t" + event.getName()+ event.getValue());

    }
}
