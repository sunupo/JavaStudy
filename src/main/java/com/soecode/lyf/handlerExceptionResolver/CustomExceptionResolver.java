package com.soecode.lyf.handlerExceptionResolver;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @date 2023-02-27
 * @author sunupo
 */
@Component
public class CustomExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {

        ex.printStackTrace();


        System.out.println("error 异常处理器正在执行中");
        ModelAndView modelAndView = new ModelAndView();
        //定义异常现象出现后，反馈给用户查看的信息
        modelAndView.addObject("message","出错了："+ex.getMessage());
        //定义异常现象出现后，反馈给用户查看的页面
        modelAndView.setViewName("error");
        return modelAndView;
    }

}

