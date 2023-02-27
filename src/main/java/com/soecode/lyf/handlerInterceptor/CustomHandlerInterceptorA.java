package com.soecode.lyf.handlerInterceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器执行顺序
 * CustomHandlerInterceptorA preHandle
 * CustomHandlerInterceptorB preHandle
 * CustomHandlerInterceptorC preHandle
 * CustomHandlerInterceptorC postHandle
 * CustomHandlerInterceptorB postHandle
 * CustomHandlerInterceptorA postHandle
 * CustomHandlerInterceptorC afterCompletion
 * CustomHandlerInterceptorB afterCompletion
 * CustomHandlerInterceptorA afterCompletion
 *
 * B return false
 * CustomHandlerInterceptorA preHandle
 * CustomHandlerInterceptorB preHandle
 * CustomHandlerInterceptorA afterCompletion
 *
 * C return false
 * CustomHandlerInterceptorA preHandle
 * CustomHandlerInterceptorB preHandle
 * CustomHandlerInterceptorC preHandle
 * CustomHandlerInterceptorB afterCompletion
 * CustomHandlerInterceptorA afterCompletion
 */

public class CustomHandlerInterceptorA implements HandlerInterceptor {
    /**
     * controller执行前调用此方法
     * 返回true表示继续执行，返回false中止执行
     * 这里可以加入登录校验、权限拦截等
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        System.out.println("CustomHandlerInterceptorA preHandle");
        return true;
    }

    /**
     * controller执行后但未返回视图前调用此方法
     * 这里可在返回用户前对模型数据进行加工处理，比如这里加入公用信息以便页面显示
     */
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("CustomHandlerInterceptorA postHandle");

    }

    /**
     * controller执行后且视图返回后调用此方法
     * 这里可得到执行controller时的异常信息
     * 这里可记录操作日志，资源清理等
     */

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        System.out.println("CustomHandlerInterceptorA afterCompletion");
    }
}
