[springmvc自定义拦截器、异常处理 - 生活的样子就该是那样 - 博客园](https://www.cnblogs.com/rhy2103/p/16497645.html)

## 请求处理过程解析

请求先被Tomcat进行处理，先经过过滤器，过滤器处理完成之后，才轮到springmvc登场，转给springMVC的DispatcherServlet核心处理单元，处理器进行处理，DispatcherServlet接收请求，按照请求对应关系分发给对应的Handler进行处理，处理完成后转换成ModelAndView进行渲染出来，为了加权限校验，在前面添加拦截器，对处理器进行公共加权限和释放权限，进行统一处理(因为AOP没有响应的功能，因此开发了与AOP相似原理的拦截器)

## 跨域访问支持

名称：@CrossOrign  
类型：方法注解、类注解  
位置：处理器类中的方法上方或类上方  
作用：设置当前处理器方法/处理器中所有方法支持跨域访问  
范例：

```
@RequestMapping("/cross")
@ResponseBody
//使用@CrossOrigin开启跨域访问
//标注在处理器方法上方表示该方法支持跨域访问
//标注在处理器类上方表示该处理器类中的所有处理器方法均支持跨域访问
// request.getRequestURL() 获取访问的URL 地址
@CrossOrigin
public User cross(HttpServletRequest request){
    System.out.println("controller cross..."+request.getRequestURL());
    User user = new User();
    user.setName("Jockme");
    user.setAge(39);
    return user;
}
```

其中@CrossOrigin中的2个参数：  
origins： 允许可访问的域列表  
maxAge:准备响应前的缓存持续的最大时间（以秒为单位）。

Spring Boot进行的跨域操作：

```
@Configuration
public class MyConfiguration{
@Bean
public WebMvcConfigurer corsConfigurer(){
return new WebMvcConfigurerAdapter(){
@Override
public void addCorsMapping(CorsRegistry registry){
registry.addMapping("/**");
 .allowedOrigins("http://domain2.com")
.allowedMethods("PUT", "DELETE")
.allowedHeaders("header1", "header2", "header3")
.exposedHeaders("header1", "header2")
.allowCredentials(false).maxAge(3600);
}
};
}
}
```

## 1、拦截器

拦截器（interceptor）是一种动态拦截方法调用的机制

-   作用：
    1.  在指定的方法调用前后执行预先设定好的代码
    2.  阻止原始方法的执行（因为像AOP的环绕通知不调用方法就不会执行）  
        核心思想：AOP思想  
        拦截器链：多个拦截器按照一定的顺序，对原始被调用功能进行增强

## 2、拦截器和过滤器的区别

归属不同：Filter属于Servlet技术，interceptor属于springMVC技术  
拦截内容不同：Filter对所有访问进行增强，Interceptor仅针对SpringMVC的访问进行增强

SpringMVC 内部有制作好的拦截器，我们自定义的拦截器是因为功能不够的时候，我们自己开发

## 自定义拦截器开发过程

## 1.实现HandlerInterceptor接口 有默认方法不会自动跳出实现

```
//自定义拦截器需要实现HandleInterceptor接口
public class MyInterceptor implements HandlerInterceptor {
    //处理器运行之前执行
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        System.out.println("前置运行----a1");
        //返回值为false将拦截原始处理器的运行
        //如果配置多拦截器，返回值为false将终止当前拦截器后面配置的拦截器的运行
        //true 代表向下放行
        return true;
    }

    //处理器运行之后执行
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        System.out.println("后置运行----b1");
    }

    //所有拦截器的后置执行全部结束后，执行该操作
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        System.out.println("完成运行----c1");
    }

    //三个方法的运行顺序为    preHandle -> postHandle -> afterCompletion
    //如果preHandle返回值为false，三个方法仅运行preHandle
}
```

```
<mvc:interceptors>
    <mvc:interceptor>
        // 拦截那个路径
        <mvc:mapping path="/showPage"/>
        // 那个拦截器在工作
        <bean class="com.itheima.interceptor.MyInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```

注意：配置顺序为先配置执行位置，后配置执行类

```
<mvc:interceptors>
    <!--开启具体的拦截器的使用，可以配置多个-->
    <mvc:interceptor>
        <!--配置拦截路径-->
        <!--拦截器很厉害,可以配置拦截多个路径-->
        <!--设置拦截器的拦截路径，支持*通配-->
        <!--/**         表示拦截所有映射-->
        <!--/*          表示拦截所有/开头的映射-->
        <!--/user/*     表示拦截所有/user/开头的映射-->
        <!--/user/add*  表示拦截所有/user/开头，且具体映射名称以add开头的映射-->
        <!--/user/*All  表示拦截所有/user/开头，且具体映射名称以All结尾的映射-->
        <mvc:mapping path="/*"/>
        <mvc:mapping path="/**"/>
        <mvc:mapping path="/handleRun*"/>
        <!--设置拦截排除的路径，配置/**或/*，达到快速配置的目的-->
        <mvc:exclude-mapping path="/b*"/>
        <!--指定具体的拦截器类-->
        <bean class="MyInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```

![image](SpringMVC%20%E8%AF%B7%E6%B1%82%E5%A4%84%E7%90%86%E8%BF%87%E7%A8%8B%E8%A7%A3%E6%9E%90.assets/1987072-20220802144417085-1128026174.png)

## 异常处理

## 异常处理器

实现HandlerExceptionResolver接口的实现类，配置成spring的bean，就会自动加载，并作为springMVC的异常加载类

第一种处理异常的方式自定义处理器，加载实际比较晚，在Controller接收完参数以后进行工作，在前面的异常拦截不到

HandleExceptionResolver接口（异常处理器）本质上相当于处理器，可以用于页面跳转，与拦截器工作原理相同

```
@Component
public class ExceptionResolver implements HandlerExceptionResolver {
    // 处理完异常后通过ModelAndView将结果进行返回
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        System.out.println("异常处理器正在执行中");
        ModelAndView modelAndView = new ModelAndView();
        //定义异常现象出现后，反馈给用户查看的信息
        modelAndView.addObject("msg","出错啦！ ");
        //定义异常现象出现后，反馈给用户查看的页面
        modelAndView.setViewName("error.jsp");
        return modelAndView;
    }
}
```

根据异常种类的不同，进行分门别类的管理，返回不同的信息

```
public class ExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        System.out.println("my exception is running ...."+ex);
        ModelAndView modelAndView = new ModelAndView();
        if( ex instanceof NullPointerException){
            modelAndView.addObject("msg","空指针异常");
        }else if ( ex instanceof  ArithmeticException){
            modelAndView.addObject("msg","算数运算异常");
        }else{
            modelAndView.addObject("msg","未知的异常");
        }
        modelAndView.setViewName("error.jsp");
        return modelAndView;
    }
}
```

## 注解开发异常处理器

使用注解处理异常就是通知来做

对Controller做增强的通知

第二种处理异常的方式 使用注解，加载时机比较早，在DispacherServlet加载完后进行加载

-   使用注解实现异常分类管理，对controller进行功能增强  
    名称：@ControllerAdvice相当于Controller  
    类型：方法注解 类型：类注解  
    位置：异常处理器类上方  
    作用：设置当前类为异常处理器类  
    范例：

```
@Component
@ControllerAdvice
public class ExceptionAdvice{
}
```

-   使用注解实现异常分类管理  
    名称：@ExceptionHandler相当于处理器  
    位置：异常处理器中针对指定异常进行处理的方法上方  
    作用：设置指定异常的处理方式  
    说明：处理器的方法可以设定多个

```
@ExceptionHandler(Exception.class)
@ResponseBody//不加会找界面
public String doOtherException(Exception ex){
return "出错啦，请联系管理员！";
}
```

## 自定义异常

-   异常定义格式

```
//自定义异常继承RuntimeException，覆盖父类所有的构造方法
public class BusinessException extends RuntimeException{
public BusinessException(){
}
public BusinessException(String message){
super(message);
}
public BusinessException(String message,Throwable cause){
super(message,cause);
}
public BusinessException(Throwable cause){
super(cause);
}
public BusinessException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
super(message,cause,enableSupperession,writableStackTrace);
}
}
```

-   异常触发方式

```
if(user.getName.trim().length()<4){
throw new BusinessException("用户名长度必须在2-4位之间，请重新输入");
}
```