package com.java.sjq.base.progress;


import io.netty.buffer.UnpooledDirectByteBuf;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.util.Arrays;

public class ProcessCommunication{

    public  void testFileCmd(){
        System.out.println(System.getProperty("user.dir"));  //C:\Users\sunupo\IdeaProjects\JavaStudy
        File file = new File("../test.txt");
        try {
            if(!file.exists())
                file.createNewFile();  //会创建于一个文件，在D:\TMP\Eclipse\test.txt
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        System.out.println(file.getAbsolutePath()); // C:\Users\sunupo\IdeaProjects\JavaStudy\..\test.txt

        try {
            System.out.println(file.getCanonicalPath());  // C:\Users\sunupo\IdeaProjects\test.txt
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println(file.getPath());  //..\test.txt
        System.out.println(file.getName());  //test.txt
        System.out.println(file.getAbsoluteFile());  //C:\Users\sunupo\IdeaProjects\JavaStudy\..\test.txt

        System.out.println();
    }

    public void   testRuntimeExec() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process exec;
//            执行一些命令，启动进程
        exec = rt.exec("shutdown.exe -s -t 3000");
        exec = rt.exec("shutdown.exe -a");
        exec = rt.exec("cmd /c calc");
        exec = rt.exec("mstsc");
        exec.waitFor();  //当前进程阻塞，直到调用的进程运行结束。
        System.out.println("exec.exitValue()="+exec.exitValue());  //正常结束时,子进程的返回值为0
    }

    public static void main(String[] args) {

        Runtime rt = Runtime.getRuntime();
        try {

            Process exec1 = rt.exec("javac -d . src/main/java/com/java/sjq/base/progress/D.java");  // 执行编译D.java文件的命令
            exec1.waitFor();
            System.out.println("javac exec:\t"+exec1.exitValue());

            Process exec2 = rt.exec("java com/java/sjq/base/progress/D");  // 运行D.class对应的字节码文件
            exec2.waitFor();
            System.out.println("java:\t"+exec2.exitValue());

            OutputStream outputStream = exec2.getOutputStream();
            InputStream inputStream = exec2.getInputStream();
            BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream));
            while(reader.ready()) {
                System.out.println("reader.readLine()\t"+reader.readLine());
            }
            byte[] b =new byte[100];
            inputStream.read(b);
            System.out.println("new String(b)"+new String(b,"utf-8"));
            System.out.println("查看内存");
            System.out.println(rt.freeMemory()+"\t"+rt.totalMemory()+"\t"+rt.maxMemory()/1024/8+"\t"+rt.availableProcessors());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ProcessBuilder pb = new ProcessBuilder("javac", "-d",".","src/testExcel/D.java");
        try {
            Process p = pb.start();
            p.waitFor();
            System.out.println(p.exitValue());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ProcessBuilder pb2 = new ProcessBuilder("java", "testExcel/D");
        try {
            Process p2 = pb2.start();
            p2.waitFor();
            System.out.println(p2.exitValue());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            pipedOutputStream.connect(pipedInputStream);
            pipedInputStream.connect(pipedOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
