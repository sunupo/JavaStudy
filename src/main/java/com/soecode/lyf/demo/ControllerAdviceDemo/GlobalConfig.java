package com.soecode.lyf.demo.ControllerAdviceDemo;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalConfig {
    @ModelAttribute(value = "info")
    public Map<String, String> userInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "罗贯中");
        map.put("gender", "男");
        return map;
    }

    @InitBinder("book")
    public void init1(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("book.");
    }
    @InitBinder("appointment")
    public void init2(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("appointment.");
    }
}

