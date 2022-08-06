package com.soecode.lyf.web;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.dto.Result;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/book") // url:/模块/资源/{id}/细分 /seckill/list
public class BookController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookService bookService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    private String list(Model model) {
        List<Book> list = bookService.getList();
        model.addAttribute("list", list);
        // list.jsp + model = ModelAndView
        return "list";// WEB-INF/jsp/"list".jsp
    }

    //	http://localhost:8080/ssm/book/1000/detail
    @RequestMapping(value = "/{bookId}/detail", method = RequestMethod.GET)
    @ResponseBody
    private String detail(@PathVariable("bookId") Long bookId, Model model, HttpServletRequest request) {
        Optional<HttpSession> session = Optional.ofNullable(request.getSession(false));
        session.ifPresent((s) -> System.out.println("已存在:\t"+s.getId()));
        if(!session.isPresent()){
            session =  Optional.ofNullable(request.getSession(true));

            session.ifPresent((s)-> s.setMaxInactiveInterval(-1));
            session.ifPresent((s)->  System.out.println("新建:"+s.getId()));
        };

        session =  Optional.ofNullable(request.getSession(true));
        session.ifPresent((s)->  System.out.println("再次创建:"+s.getId()));  // 已有session，再次创建不会生成新的session

        System.out.println(session.get().getServletContext().getContextPath());

        Book book = new Book(1, "Data Structure", 998);// = bookService.getById(bookId);
        model.addAttribute("book", book);
        JsonObject object = new JsonObject();
        object.addProperty("code", 0);
        object.addProperty("msg", "sunjingqin");
        JsonObject object1 = new JsonObject();
        object1.addProperty("number", book.getNumber());
        object1.addProperty("name", book.getName());
        object1.addProperty("bookId", book.getBookId());
        object.add("data", object1);
        return object.toString();
    }

    // ajax json
    @RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces = {
            "application/json; charset=utf-8"})
    @ResponseBody
    private Result<AppointExecution> appoint(@PathVariable("bookId") Long bookId, @RequestParam("studentId") Long studentId) {
        if (studentId == null || studentId.equals("")) {
            return new Result<>(false, "学号不能为空");
        }
        AppointExecution execution = null;
        try {
            execution = bookService.appoint(bookId, studentId);
        } catch (NoNumberException e1) {
            execution = new AppointExecution(bookId, AppointStateEnum.NO_NUMBER);
        } catch (RepeatAppointException e2) {
            execution = new AppointExecution(bookId, AppointStateEnum.REPEAT_APPOINT);
        } catch (Exception e) {
            execution = new AppointExecution(bookId, AppointStateEnum.INNER_ERROR);
        }
        return new Result<AppointExecution>(true, execution);
    }

}
