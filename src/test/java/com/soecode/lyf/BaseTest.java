package com.soecode.lyf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;














/**
 * 配置spring和junit整合，junit启动时加载springIOC容器 spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring配置文件
@ContextConfiguration({ "classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml" })
public class BaseTest {
    @Test
    public void list() {
        String s1 = "ab";
        String s = new String("a") + new String("b");  // 也是新建了一个StringBuilder实现字符串的拼接与创建。最终的结果就是在堆中创建了一个“ab”字符串对象。并且在串池中加入“a”，“b”对象。
        String s2 = s.intern();

        System.out.println(s1 == "ab"); // true
        System.out.println(s == "ab"); // false, 因为 s.intern()之前已经存在 "ab"，所以 s 仍然指向堆中的对象，s2作为返回值，执行串池中的对象s1="ab"
        System.out.println(s2 == "ab"); // true

        System.out.println("👽👀👍.length="+"👽👀👍".length());

    }

    public static void main(String[] args) {
        new BaseTest().list();
    }
}