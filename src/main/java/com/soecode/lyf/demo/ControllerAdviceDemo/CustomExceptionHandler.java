package com.soecode.lyf.demo.ControllerAdviceDemo;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 如果想让该方法处理所有类型的异常，只需将MaxUploadSizeExceededException改为 Exception即可。
 * 方法的参数可以有异常实例、HttpServletResponse以及HttpServletRequest、Model 等，
 * 返回值可以是一段JSON、一个ModelAndView、一个逻辑视图名等。
 * ————————————————
 * 版权声明：本文为CSDN博主「探索er」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/qq_43581790/article/details/123871439
 */
@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void uploadException(MaxUploadSizeExceededException e, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        System.out.println(1111);
        PrintWriter out = resp.getWriter();
        out.write("上传文件大小超出限制!");
        out.flush();
        out.close();
    }
}

