package com.java.sjq.base.JNDI;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args){
      //
        Connection conn=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Context ctx=new InitialContext();
            Object datasourceRef=ctx.lookup("java:comp/env/jndi/person"); //引用数据源
            DataSource ds=(DataSource)datasourceRef;
            conn=ds.getConnection();
            String sql = "select * from person where id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "1");
            rs = ps.executeQuery();
            while(rs.next()){
                System.out.println("person name is "+rs.getString("name"));
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
