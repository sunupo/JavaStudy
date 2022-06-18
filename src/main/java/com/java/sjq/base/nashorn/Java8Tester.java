package com.java.sjq.base.nashorn;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;

public class Java8Tester {
    public static void main(String args[]){

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");

        String name = "Runoob";
        Integer result = null;

        try {
            nashorn.eval("print('" + name + "')");
            result = (Integer) nashorn.eval("10 + 2");

        }catch(ScriptException e){
            System.out.println("执行脚本错误: "+ e.getMessage());
        }

        System.out.println(result.toString());
    }

    @Test
    public void fun() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        // 通过Runtime对象执行ls命令
        Process ifconfig = runtime.exec("jjs src/main/java/com/java/sjq/base/nashorn/sample.js");

        // 通过Process获取输入流对象
        InputStream inputStream = ifconfig.getInputStream();
        byte[] arr = new byte[1024 * 1024 * 100];
        // 读取数据，返回读取到的字节个数
        int len = inputStream.read(arr);
        // 将字节转化为字符串输出到控制台
        System.out.println(new String(arr, 0, len, "GBK"));

    }
}
