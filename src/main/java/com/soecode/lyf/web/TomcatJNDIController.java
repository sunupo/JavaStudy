package com.soecode.lyf.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soecode.lyf.AttributeListener.SSMAttributeListener;
import com.soecode.lyf.convert.CustomDateConverter;
import com.soecode.lyf.entity.Appointment;
import org.apache.camel.Converter;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.dto.Result;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/book") // url:/模块/资源/{id}/细分 /seckill/list
public class BookController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookService bookService;
    private Optional<HttpSession> session;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    private String list(Model model) {
        List<Book> list = bookService.getList();
        model.addAttribute("list", list);
        // list.jsp + model = ModelAndView
        return "list";// WEB-INF/jsp/"list".jsp
    }
    @RequestMapping(value = "/post/detail/{bookId}", method = RequestMethod.GET)
    @ResponseBody
    private void postDetail(@PathVariable(value = "bookId") Long bookId, HttpServletRequest request,
                            @RequestParam(value="bookName",required=true) String name) throws IOException {

        System.out.println("bookId"+ bookId +"\t bookName" + name);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {

            System.out.println("line" + line);
        }
        if (bookId==-1) throw new RepeatAppointException("dsds");
    }
    @RequestMapping(value = "/getIP")
    @ResponseBody
    private String getIP(HttpServletRequest request) throws IOException {
        System.out.println(request.getRemoteAddr());
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            System.out.println("1:" + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            System.out.println("2:" + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            System.out.println("3:" + ip);
        }
        System.out.println(ip);
        return "";

    }

    @RequestMapping(value = "/testSession", method = RequestMethod.GET)
    @ResponseBody
    private String testSession(HttpServletRequest request) {

//   这儿写了，就不用在web.xml 注册SSMAttributeListener
//        web.xml 注册SSMAttributeListener应该是全局起作用

//        session.ifPresent((s) -> {
////            s.getServletContext().addListener(new SSMAttributeListener());
//            Book book = (Book) s.getAttribute("bind math book");
//            System.out.println("controller" + book.toString());
//        });

        System.out.println("--------");
        Optional<HttpSession> session = Optional.ofNullable(request.getSession(false));
        session.ifPresent((s) -> System.out.println("已存在:\t" + s.getId()));
        if (!session.isPresent()) {
            session = Optional.ofNullable(request.getSession(true));
            String bookName = session.get().getId();

            session.ifPresent((s) -> s.setMaxInactiveInterval(-1));
            session.ifPresent((s) -> System.out.println("新建:" + s.getId()));
            session.ifPresent((s) -> {
                Book book = new Book(2, bookName, 12);
                s.setAttribute("bind-math-book", book);

                Appointment appointment = new Appointment(2, 3, new Date());
                s.setAttribute("bind-appointment", appointment);
            });

        } else {
            session.ifPresent((s) -> s.setAttribute("bind-math-book", new Book(3, s.getId().substring(0,5), 12)));
            session.ifPresent((s) -> s.removeAttribute("bind-appointment"));
        }

        session = Optional.ofNullable(request.getSession(true));
        session.ifPresent((s) -> System.out.println("再次创建:" + s.getId()));  // 已有session，再次创建不会生成新的session
        return "testSession";
        /**
         * 以下是第一次打开网页和接着刷新网页的输出。
         *
         * 新建:FA3C3B6467BDFEE969369380842993D4
         * book valueBound:	bind-math-book:Book [bookId=2, name=FA3C3B6467BDFEE969369380842993D4, number=12]
         * HttpSessionAttributeListener	attributeAdded:	bind-math-bookBook [bookId=2, name=FA3C3B6467BDFEE969369380842993D4, number=12]
         * appoint valueBound:	bind-appointment:Appointment [bookId=2, studentId=3, appointTime=Fri Feb 24 16:13:55 CST 2023]
         * HttpSessionAttributeListener	attributeAdded:	bind-appointmentAppointment [bookId=2, studentId=3, appointTime=Fri Feb 24 16:13:55 CST 2023]
         * 再次创建:FA3C3B6467BDFEE969369380842993D4
         * CustomEventListenerServletRequestHandledEvent: url=[/ssm_war_exploded2/book/testSession]; client=[0:0:0:0:0:0:0:1]; method=[GET]; servlet=[mvc-dispatcher]; session=[FA3C3B6467BDFEE969369380842993D4]; user=[null]; time=[11ms]; status=[OK]
         *
         *
         * --------
         * 已存在:	FA3C3B6467BDFEE969369380842993D4
         * book valueBound:	bind-math-book:Book [bookId=3, name=FA3C3, number=12]
         * book valueUnbound: 	bind-math-book:null
         * HttpSessionAttributeListener	attributeReplaced:	bind-math-bookBook [bookId=3, name=FA3C3, number=12]
         * appoint valueUnbound:	bind-appointment:Appointment [bookId=2, studentId=3, appointTime=Fri Feb 24 16:13:55 CST 2023]
         * HttpSessionAttributeListener	attributeRemoved:	bind-appointmentAppointment [bookId=2, studentId=3, appointTime=Fri Feb 24 16:13:55 CST 2023]
         * 再次创建:FA3C3B6467BDFEE969369380842993D4
         * CustomEventListenerServletRequestHandledEvent: url=[/ssm_war_exploded2/book/testSession]; client=[0:0:0:0:0:0:0:1]; method=[GET]; servlet=[mvc-dispatcher]; session=[FA3C3B6467BDFEE969369380842993D4]; user=[null]; time=[8ms]; status=[OK]
         */
    }

    @RequestMapping(value = "/testSession2", method = RequestMethod.GET)
    @ResponseBody
    private String testSession2(HttpServletRequest request) {
        testSession(request);
        return "testSession2";
    }

        //	http://localhost:8080/ssm/book/detail
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    private String detail(@PathVariable("bookId") Long bookId, Model model, HttpServletRequest request) throws IOException {


        Book book = new Book(1, "Data Structure", 998);// = bookService.getById(bookId);
        model.addAttribute("book", book);
        JsonObject object = new JsonObject();
        object.addProperty("code", 0);
        object.addProperty("msg", "sunjingqin");
        JsonObject object1 = new JsonObject();
        object1.addProperty("number", book.getNumber());
        object1.addProperty("name", book.getName());
        object1.addProperty("bookId", book.getBookId());
        object1.addProperty("bookId2", new Book().getBookId());
        object1.addProperty("name2", new Book().getName());
        object1.addProperty("enough2", new Book().isEnough());
        ArrayList<Book> books = new ArrayList<>();
//        books.add(book);
        books.add(new Book());
        JsonArray jsonArray = new JsonArray();
        object1.addProperty("list1", jsonArray.toString());

        for (Book b : books) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("name", b.getName());
            jObj.addProperty("bookId", b.getBookId());
            jObj.addProperty("number", b.getNumber());
            jObj.addProperty("enough", b.isEnough());
            jsonArray.add(jObj);
            System.out.println(b.toString());
        }
        object1.addProperty("list2", jsonArray.toString());
        object.add("data", object1);
        return object.toString();
    }

    @RequestMapping(value = "/detail2", method = RequestMethod.GET)
    @ResponseBody
    private Object detail2() {
        Book book = new Book(1, "Data Structure", 998);// = bookService.getById(bookId);
        List<Book> res = new ArrayList<Book>();
        res.add(book);
        res.add(book);
        res.add(book);
        return res;
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

    /**
     * http://localhost:8080/ssm_war_exploded2/book/testControllerAdvice1?book.bookId=1&book.name=namebb&appointment.bookId=2
     * @param book
     * @param appointment
     * @param model
     */
    @RequestMapping(value = "/testControllerAdvice1")
    @ResponseBody
    private void testControllerAdvice1(@ModelAttribute("book")Book book, @ModelAttribute("appointment")Appointment appointment, Model model){
        System.out.println("Book appointment"+book+appointment);
        Map<String, Object> map = model.asMap();
        Set<String> keySet = map.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = map.get(key);
            System.out.println(key + ">>>>>" + value);
        }
    }
    @RequestMapping(value = "/testControllerAdvice2/{fileSize}")
    @ResponseBody
    private int testControllerAdvice( @PathVariable("fileSize") Integer fileSize){
        if(fileSize>100){
            throw new MaxUploadSizeExceededException(fileSize);
        }
        return fileSize;
    }

    @RequestMapping(value = "/convertDate")
    @ResponseBody
    private String testControllerAdvice( @RequestParam(value = "time", required = true) Date time){
        return time.toString();
    }

    /**
     *
     * @param book
     * @return
     */
    @RequestMapping(value = "/testJSON", method = RequestMethod.POST)
    @ResponseBody
    private Book testJSON(@RequestBody Book book){
        System.out.println(book.toString());
        book.setBookId(998);
        return book;
    }

}
