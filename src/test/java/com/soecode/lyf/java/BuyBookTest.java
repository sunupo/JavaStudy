package com.soecode.lyf.java;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.gson.Gson;
import junit.framework.TestCase;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.proxy.*;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


class BuyBook{


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String name;
    public BuyBook() {}
    public BuyBook(String name) {
        this.name = name;
    }
    public void buy(){
        System.out.println("buy: book");
        logger.debug("buy: book");
    }
    public void buy2(){
        System.out.println("buy2: book");
        logger.debug("buy2:book");
    }
    public int sell(int price){
        logger.debug("sell: " + price);
        return price;
    }
    @Override
    public String toString() {
        return "BuyBook{}"+getClass().getName();
    }
}


public class BuyBookTest extends TestCase {
    /*
    测试cglib
     */
    @Test
    public void testCglibProxy(){
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(BuyBook.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            System.out.println("testCglibProxy事务开始......" + method.getName());
            Object o1 = proxy.invokeSuper(obj, args);
            System.out.println("testCglibProxy事务结束......." + method.getName());
            return o1;
        });
        BuyBook buyBook = (BuyBook) enhancer.create();
        buyBook.buy();
    }

    /*
    测试CallBackFilter
     */
    @Test
    public void testCallBackfilter(){
        Enhancer enhancer =new Enhancer();
        enhancer.setSuperclass(BuyBook.class);
        CallbackFilter callbackFilter = new CallbackFilter(){
            @Override
            public int accept(Method method) {
                //返回值是整数，对应着在下文创建CallBack数组中的元素。比如："Buy"返回0，意味着使用CallBack数组中第0个，也就是Inteceptor
                switch (method.getName()){
                    case "buy":
                        System.out.println("testCallBackfilter-method:buy");
                        return 0;
                    case "buy2":
                        System.out.println("testCallBackfilter-method:buy2");
                        return 1;
                    case "sell":
                        System.out.println("testCallBackfilter-method:sell");
                        return 2;
                    case "toString":
                        System.out.println("testCallBackfilter-method:toString");
                        return 3;
                    default:
                        return 0;
                }
            }
        };

        class Inteceptor implements MethodInterceptor{
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("Inteceptor事务开始......" + method.getName());
                Object o1 = proxy.invokeSuper(obj, args);
                System.out.println("Inteceptor事务结束......." + method.getName());
                return o1;
            }
        }

        //数组中的元素都是Callback的实例
        Callback[] cbarray = new Callback[]{
                new Inteceptor(),
                NoOp.INSTANCE,
                (FixedValue) () -> {
                    System.out.println("返回固定结果");
                    return 100;
                },
                NoOp.INSTANCE
        };
        enhancer.setCallbacks(cbarray);
        enhancer.setCallbackFilter(callbackFilter);
        BuyBook buyBook = (BuyBook) enhancer.create();

        System.out.println("开始=========");
        System.out.println(buyBook); //会调用toString
        System.out.println("0==========\n");

        buyBook.buy();
        System.out.println("1========\n");

        buyBook.buy2();
        System.out.println("2=-=-=-=-=-\n");

        System.out.println(buyBook.sell(100));
        System.out.println("3-=-=-=-=-=\n");

        System.out.println(buyBook.toString()); // 会调用toString

    }

    /**
     * BeanGenerator 实例
     */
    public void testbeanGenerator(){
        BeanGenerator beanGenerator = new BeanGenerator();
        try {

            beanGenerator.addProperty("name", String.class); //属性名称XXX
            Object target = beanGenerator.create();
            Method setter = target.getClass().getDeclaredMethod("setName", String.class);  //形如setXXX
            Method getter = target.getClass().getDeclaredMethod("getName");
            // 设置属性的值
            setter.invoke(target, "张三");
            //读取属性的值
            System.out.println(getter.invoke(target));

            //通过反射，读取创建的bean的方法
            Class<?> clazz = target.getClass();
            System.out.println(clazz.getName());
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                System.out.println(methods[i].getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFastClass(){
        //1⃣️通过BuyBook.class创建FastClass实例
        FastClass fastClass = FastClass.create(BuyBook.class);

        try {
//            2⃣️创建BuyBook实例（）
//            BuyBook buyBook = new BuyBook();
            BuyBook buyBook = (BuyBook) fastClass.newInstance(new Class[]{String.class}, new Object[]{"JAVA BOOK"});
//            3⃣️调用方法1
            fastClass.invoke("sell", new Class[]{int.class}, buyBook,new Object[]{100});
//            3⃣️调用方法2
            FastMethod fastMethod = fastClass.getMethod("sell", new Class[]{int.class});
            fastMethod.invoke(buyBook, new Object[]{300});
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testDelete(){
        //会报错
            List<String> list = new ArrayList<>();
            list.add("1");
            list.add("2");
            list.add("3");
            list.add("4");
            list.add("5");
            System.out.println(list);
//            Iterator<String> iterator = list.iterator();
            for (String s : list) {
                int index = list.indexOf("4");
                list.remove(index);
            }
            System.out.println(list);

    }
    @Test
    public void testDelete1(){
        //不报错，但是结果不对

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        list.add("4");
        list.add("5");
        System.out.println(list);
        list.remove("4");//只会删除一个4
        System.out.println(list);
    }

    @Test
    public void testDelete2(){
        //不报错，但是结果不对,第二个4无法删除

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        list.add("4");
        list.add("5");
        list.add("4");
        System.out.println(list);
        for (int i = 0; i < list.size(); i++) {
            if("4".equals(list.get(i))) {
                list.remove(i);//
//                --i;//下标减一就对了
            }
        }
        System.out.println(list);
    }

    @Test
    public  void testDd() {
        //采用迭代器iterator删除，正确
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        list.add("4");
        list.add("5");
        list.add("4");
        System.out.println(list);
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String s = iterator.next();
            if ("4".equals(s)) {
                iterator.remove();
            }
        }
        System.out.println(list);
    }
    @Test
    public  void testDdd() {
        //报错， 采用iterator迭代过程中，使用原始的list对象删除元素 也会报错
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("4");
        list.add("4");
        list.add("5");
        list.add("4");

        System.out.println(list);
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String s = iterator.next();
            if ("4".equals(s)) {
                list.remove(s);
            }
        }
        System.out.println(list);
    }
    @Test(timeout = 1000)
    public void testNet() throws IOException {
        URL url = new URL("https://cn.bing.com/search?q=fd");
        URLConnection connection = url.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

        httpURLConnection.setRequestMethod("GET");  //设置一些属性
        Gson gson=new Gson();


        InputStream inputStream = httpURLConnection.getInputStream();//这个过程可能会阻塞，得到服务器响应的数据

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);//对输入流进行包装
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        while((line=reader.readLine())!=null){
            System.out.println(line);
        }

        //关闭输入流对象
        while (reader!=null){
            reader.close();
            reader=null;
        }
        while(inputStreamReader!=null){
            inputStreamReader.close();
            inputStreamReader=null;
        }
        while(inputStream!=null){
            inputStream.close();
            inputStream=null;
        }
    }
    @Test
    public void testWeak(){
        WeakReference<byte[]> wb = new WeakReference<byte[]>(new byte[1024*1024*5]);
        System.out.println(wb.get());
        byte[] b = new byte[1024*1024*600];
        System.out.println(wb.get());

        String str = "";
        boolean flag=false;
        str = str + (flag? "1":"2");
        System.out.println(str);

    }

    public static void main(String[] args) {
        class Inner{
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                System.out.println("finalize()");
            }
        }
        Inner inner= new Inner();
        inner=null;
        System.gc();
//        try {
//            Thread.sleep(0);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(inner);


        WeakReference<byte[]> wb = new WeakReference<byte[]>(new byte[1024*1024*5]);
        System.out.println(wb.get());
        byte[] b = new byte[1024*1024*6];
        System.out.println(wb.get());
        byte[] bytes = wb.get();
//        for (int i = 0; i < bytes.length; i++) {
//
//        }
        System.out.println(b);
        System.out.println(wb.get());
        System.out.println("12");
        Calendar calendar;
        calendar = Calendar.getInstance();
        System.out.println("1");

        System.out.println(calendar.getTimeInMillis());

    }

    public void fun234(){
        System.out.println("12");
        Calendar calendar;
        calendar = Calendar.getInstance();
        System.out.println("1");

        System.out.println(calendar.getTimeInMillis());
    }
    class Bean{
        private String name;
        private int poiId;
        private String poiName;
        private int boothId;
        private String boothName;
        private int guestWayID;
        private String guestWatDesc;
        private String protocolUrl;
        private boolean scheduling;
        private Date qrCodeEffectiveMils;
        private Calendar calendar;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPoiId() {
            return poiId;
        }

        public void setPoiId(int poiId) {
            this.poiId = poiId;
        }

        public String getPoiName() {
            return poiName;
        }

        public void setPoiName(String poiName) {
            this.poiName = poiName;
        }

        public int getBoothId() {
            return boothId;
        }

        public void setBoothId(int boothId) {
            this.boothId = boothId;
        }

        public String getBoothName() {
            return boothName;
        }

        public void setBoothName(String boothName) {
            this.boothName = boothName;
        }

        public int getGuestWayID() {
            return guestWayID;
        }

        public void setGuestWayID(int guestWayID) {
            this.guestWayID = guestWayID;
        }

        public String getGuestWatDesc() {
            return guestWatDesc;
        }

        public void setGuestWatDesc(String guestWatDesc) {
            this.guestWatDesc = guestWatDesc;
        }

        public String getProtocolUrl() {
            return protocolUrl;
        }

        public void setProtocolUrl(String protocolUrl) {
            this.protocolUrl = protocolUrl;
        }

        public boolean isScheduling() {
            return scheduling;
        }

        public void setScheduling(boolean scheduling) {
            this.scheduling = scheduling;
        }

        public Date getQrCodeEffectiveMils() {
            return qrCodeEffectiveMils;
        }

        public void setQrCodeEffectiveMils(Date qrCodeEffectiveMils) {
            this.qrCodeEffectiveMils = qrCodeEffectiveMils;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
        //        "name":"孙敬钦"
//                "poiId":2
//                "poiName":"美团买菜-北京博泰店-BJ0001"
//                "boothId":1339
//                "boothName":"慧谷根园西1门"
//                "guestWayId":1
//                "guestWayDesc":"新人专区买赠拉新"
//                "protocolUrl":"/pages/pullNew/index?poiid=2&subjectid=1&boothid=1339&mobile=13228303720&type=1&bizid=2"
//                "scheduling":false
//                "qrCodeEffectiveMils":0

    }
}

