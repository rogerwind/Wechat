package com.roger.kxmoment.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class MySqlUtil {
	private static Logger log=Logger.getLogger(MySqlUtil.class);
	public  Connection getConnection() throws Exception{
		
		//String host=request.getHeader("BAE_ENV_ADDR_SQL_IP");									
		//String port=request.getHeader("BAE_ENV_ADDR_SQL_PORT");					
		//String username=request.getHeader("BAE_ENV_AK");									
		//String password=request.getHeader("BAE_ENV_SK");
		
		ResourceBundle resourceBundle=ResourceBundle.getBundle("database_info");
				
		String dbName=resourceBundle.getString("dbName");
		String host = resourceBundle.getString("host");
		String port = resourceBundle.getString("port");
		String username=resourceBundle.getString("username");
		String password=resourceBundle.getString("password");
		
		String url=String.format("jdbc:mysql://%s:%s/%s", host,port,dbName);
		log.info("MySql database url: "+url);	
		
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(url, username, password);
		
	}
}
