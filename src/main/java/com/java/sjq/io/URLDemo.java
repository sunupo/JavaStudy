package com.java.sjq.io;

import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 直接通过 URL 读取字节流数据
 */

public class URLDemo {
    public static void main(String[] args) throws Exception {
//        URL url = new URL("http://www.baidu.com");
        URL url = new URL("http://www.cyc2018.xyz/Java/Java%20IO.html#transient");

        /* 字节流 */
        InputStream is = url.openStream();

        /* 字符流 */
        InputStreamReader isr = new InputStreamReader(is, "utf-8");

        /* 提供缓存功能 */
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        br.close();
    }

    @Test
    public void testObjectSerializable() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        A a1 = new A(123, "abc", list);
        String objectFile = "out/" + this.getClass().getName()+ "." + Thread.currentThread().getStackTrace()[1].getMethodName();
        System.out.println(objectFile);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
        objectOutputStream.writeObject(a1);
        objectOutputStream.close();

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(objectFile));
        A a2 = (A) objectInputStream.readObject();
        objectInputStream.close();
        System.out.println(a2);
    }

    @Test
    public void testListSerializable() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        String objectFile = "out/" + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
        ;
        System.out.println(objectFile);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
        objectOutputStream.writeObject(list);
        objectOutputStream.close();

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(objectFile));
        Object objList = objectInputStream.readObject();
//        List<String> res = (ArrayList<String>) objectInputStream.readObject(); // 列表不能直接反序列化，会告警
        List<String> res = new ArrayList<String>();
        if (objList instanceof ArrayList<?>) {
            ((ArrayList<?>) objList).forEach((obj) -> {
                res.add((String) obj);
            });
        }
        objectInputStream.close();
        System.out.println(res);
    }

    private static class A implements Serializable {

        private int x;
        private String y;
        private List<String> z;

        A(int x, String y, List<String> z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "x = " + x + "  " + "y = " + y + "  z = " + z;
        }
    }
}
