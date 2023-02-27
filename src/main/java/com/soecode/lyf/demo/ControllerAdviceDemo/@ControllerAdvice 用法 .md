[(119条消息) @ControllerAdvice 用法\_探索er的博客-CSDN博客](https://blog.csdn.net/qq_43581790/article/details/123871439)
## @[ControllerAdvice](https://so.csdn.net/so/search?q=ControllerAdvice&spm=1001.2101.3001.7020) 用法

顾名思义，@ControllerAdvice就是@Controller 的增强版。@ControllerAdvice主要用来处理全局数据，一般搭配@[ExceptionHandler](https://so.csdn.net/so/search?q=ExceptionHandler&spm=1001.2101.3001.7020)、@ModelAttribute以及@InitBinder使用。

## 全局异常处理

@ControllerAdvice最常见的使用场景就是全局异常处理。比如文件上传大小限制的配置，如果用户上传的文件超过了限制大小，就会抛出异常,此时可以通过@ControllerAdvice结合@ExceptionHandler定义全局异常捕获机制，代码如下:

```
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
```

只需在系统中定义CustomExceptionHandler类，然后添加@ControllerAdvice注解即可。当系统启动时，该类就会被扫描到Spring容器中，然后定义uploadException方法，在该方法上添加了@ExceptionHandler注解，其中定义的MaxUploadSizeExceededException.class 表明该方法用来处理MaxUploadSizeExceededException类型的异常。如果想让该方法处理所有类型的异常，只需将MaxUploadSizeExceededException改为 Exception即可。方法的参数可以有异常实例、HttpServletResponse以及HttpServletRequest、Model 等，返回值可以是一段JSON、一个ModelAndView、一个逻辑视图名等。此时，上传一个超大文件会有错误提示给用户。

![在这里插入图片描述](https://img-blog.csdnimg.cn/b0b993c5518143d9b0baa8cb57ccbd7e.png#pic_center)

如果返回参数是一个ModelAndView，假设使用的页面模板为Thymeleaf(注意添加Thymeleaf相关依赖）,此时异常处理方法定义如下:

```
@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView uploadException(MaxUploadSizeExceededException e) throws IOException {
        ModelAndView mv = new ModelAndView();
        mv.addObject("msg", "上传文件大小超出限制! ");
        mv.setViewName("error");
        return mv;
    }
}
```

然后在resources/templates目录下创建error.html文件，内容如下:

```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title></head>
<body>
<div th:text="${msg}"></div>
</body>
</html>
```

此时上传出错效果一致。

## 添加全局数据

@ControllerAdvice是一个全局数据处理组件，因此也可以在@ControllerAdvice中配置全局数据，使用@[ModelAttribute](https://so.csdn.net/so/search?q=ModelAttribute&spm=1001.2101.3001.7020)注解进行配置，代码如下:

```
@ControllerAdvice
public class GlobalConfig {
    @ModelAttribute(value = "info")
    public Map<String, String> userInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "罗贯中");
        map.put("gender", "男");
        return map;
    }
}
```

代码解释:

-   在全局配置中添加userInfo方法，返回一个map。该方法有一个注解@ModelAttribute，其中的value属性表示这条返回数据的key，而方法的返回值是返回数据的value。
-   此时在任意请求的Controller 中，通过方法参数中的Model都可以获取info 的数据。

Controller 例代码如下：

```
public class MyController {
    @GetMapping("/hello")
    @ResponseBody
    public void hello(Model model) {
        Map<String, Object> map = model.asMap();
        Set<String> keySet = map.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = map.get(key);
            System.out.println(key + ">>>>>" + value);
        }
    }
}
```

在请求方法中，将Model 中的数据打印出来，如图所示。

![在这里插入图片描述](https://img-blog.csdnimg.cn/1396a04999a74653a9a272bf9924025c.png#pic_center)

## 请求参数预处理

@ControllerAdvice结合@InitBinder还能实现请求参数预处理，即将表单中的数据绑定到实体类上时进行一些额外处理。

例如有两个实体类 Book和 Author，代码如下:

```
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ToString
public class Book {
    private String name;
    private String author;
    @JsonIgnore//一般标记在属性或者方法上，返回的json数据即不包含该属性
    private Float price;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publicationDate;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Author {
    private String name;
    private int age;
}
```

在 Controller 上需要接收两个实体类的数据，Controller 中的方法定义如下:

```
@ControllerAdvice
public class GlobalConfig1 {
    @InitBinder("b")
    public void init(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("b.");
    }

    @InitBinder("a")
    public void init2(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("a.");
    }
}
```

代码解释:

-   在 GlobalConfig类中创建两个方法，第一个@InitBinder(“b”)表示该方法是处理@ModelAttribute(“b”)对应的参数的，第二个@InitBinder(“a”)表示该方法是处理@ModelAttribute(“a”)对应的参数的。
-   在每个方法中给相应的 Field设置一个前缀，然后在浏览器中请求http:/ocalhost:8080/book?b.name=三国演义&b.author=罗贯中&a.name=曹雪芹&a.age=48，即可成功地区分出name属性。
-   在WebDataBinder对象中，还可以设置允许的字段、禁止的字段、必填字段以及验证器等。

ld设置一个前缀，然后在浏览器中请求http:/ocalhost:8080/book?b.name=三国演义&b.author=罗贯中&a.name=曹雪芹&a.age=48，即可成功地区分出name属性。

-   在WebDataBinder对象中，还可以设置允许的字段、禁止的字段、必填字段以及验证器等。

![在这里插入图片描述](https://img-blog.csdnimg.cn/3c82667c18194d52813f253587a5611b.png#pic_center)

[(119条消息) SpringBoot @ControllerAdvice @InitBinder 的使用详解\_Full Stack Developme的博客-CSDN博客\_controlleradvice initbinder](https://blog.csdn.net/lizhengyu891231/article/details/120867772)
## 叙述

**我们知道无论是 Get 请求还是 Post 请求，Controller 这边都可以定义一个实体类来接收这些参数。而 @ControllerAdvice 结合 @InitBinder 还能实现请求参数预处理，即将表单中的数据绑定到实体类上时进行一些额外处理。**

## 描述

假设我们有如下两个实体类 User 和 Book：

```
public class User {private String name;private Integer age;}public class Book {private String name;private Float price;}
```

如果在 Contoller 上需要接收两个实体类的数据，接收方法可以这么定义：

```
import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.RestController;@RestControllerpublic class HelloController {@GetMapping("/hello")public String hello(User user, Book book) {return "name：" + user.getName() + " | age：" + user.getAge() + "<br>"                + "name：" + book.getName() + " | price：" + book.getPrice();    }}
```

但由于两个实体类中都有 name 属性，那么参数传递时就会发生混淆。

![原文:SpringBoot - @ControllerAdvice的使用详解3（请求参数预处理 @InitBinder）](https://img-blog.csdnimg.cn/img_convert/86bdc6d9d4b7aee96f7846378bc2e067.png)

##  解决方案

使用 @ControllerAdvice 结合 @InitBinder 即可解决上面的问题，这里我们创建一个全局的参数预处理配置。

> **代码说明：**
>
> -   **第一个 @InitBinder("user") 表示该方法是处理 Controller 中 @ModelAttribute("user") 对应的参数。**
> -   **第二个 @InitBinder("book") 表示该方法是处理 Controller 中 @ModelAttribute("book") 对应的参数。**
> -   **这两个方法中给相应的 Filed 设置一个前缀。**
>
> **补充说明：在 WebDataBinder 对象中，除了可以设置前缀，还可以设置允许、禁止的字段、必填字段以及验证器等等。**

```
import org.springframework.web.bind.WebDataBinder;import org.springframework.web.bind.annotation.ControllerAdvice;import org.springframework.web.bind.annotation.InitBinder;@ControllerAdvicepublic class GlobalConfig {@InitBinder("user")public void init1(WebDataBinder binder) {        binder.setFieldDefaultPrefix("user.");    }@InitBinder("book")public void init2(WebDataBinder binder) {        binder.setFieldDefaultPrefix("book.");    }}
```

然后 Controller 中方法的参数添加 @ModelAttribute 注解：

```
import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.ModelAttribute;import org.springframework.web.bind.annotation.RestController;@RestControllerpublic class HelloController {@GetMapping("/hello")public String hello(@ModelAttribute("user") User user,@ModelAttribute("book") Book book) {return "name：" + user.getName() + " | age：" + user.getAge() + "<br>"                + "name：" + book.getName() + " | price：" + book.getPrice();    }}
```

最后浏览器请求参数中添加相应的前缀，即可成功区分出 name 属性：

![原文:SpringBoot - @ControllerAdvice的使用详解3（请求参数预处理 @InitBinder）](https://img-blog.csdnimg.cn/img_convert/85c1a83e1247af5fb34827337f6f75a3.png)