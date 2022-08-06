package com.java.sjq.base.sql.mysql;

import java.sql.*;
// https://blog.csdn.net/baiwenjiebwj/article/details/122099407
//mac之idea连接MySQL数据库报com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
public class MysqlDemo {

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/ssm";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://11.2.37.18:3306/ssm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
//    static final String DB_URL = "jdbc:mysql://11.2.37.18:3306/ssm?useSSL=false&serverTimezone=UTC";
//    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/ssm?useSSL=false";


    // 数据库的用户名与密码，需要根据自己的设置s
    static final String USER = "sunjingqin";
    static final String PASS = "123456";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT book_id, name, number FROM book";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("book_id");
                String name = rs.getString("name");
                String url = rs.getString("number");

                // 输出数据
                System.out.print("ID: " + id);
                System.out.print(", name: " + name);
                System.out.print(", number: " + url);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}
