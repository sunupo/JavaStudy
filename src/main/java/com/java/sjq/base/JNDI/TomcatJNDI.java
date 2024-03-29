package com.java.sjq.base.JNDI;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TomcatJNDI {

   public static void runTomcatJNDI(String name /*jndi/user*/, int  id){
      //
        Connection conn=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Context ctx=new InitialContext();
            Context datasourceRef=(Context)ctx.lookup("java:comp/env/"); //引用数据源
            DataSource ds=(DataSource)datasourceRef.lookup(name);
            conn=ds.getConnection();
            String sql = "select * from user where id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "1");
            rs = ps.executeQuery();
            while(rs.next()){
                System.out.println("user id is "+rs.getString("id"));
                System.out.println("user name is "+rs.getString("username"));
            }
            ctx.close();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch(SQLException e) { }
            }
        }
    }
}

/**
 * 1. 新建一个Web项目，在META-INF目录下新建context.xml文件
 <?xml version="1.0" encoding="UTF-8"?>
 <Context>
 <Resource name="jndi/person"
 auth="Container"
 type="javax.sql.DataSource"
 username="root"
 password="root"
 driverClassName="com.mysql.jdbc.Driver"
 url="jdbc:mysql://localhost:3306/test"
 maxTotal="8"
 maxIdle="4"/>
 </Context>

 */
