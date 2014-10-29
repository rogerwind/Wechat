package com.roger.kxmoment.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.roger.kxmoment.response.TextMessage;
import com.roger.kxmoment.util.MessageUtil;
import com.roger.kxmoment.util.MySqlUtil;
import com.roger.kxmoment.util.StopWatch;


public class CoreService {
	
	private static Logger log = Logger.getLogger(CoreService.class);
	/**
	 * 处理微信发来的请求
	 * 
	 * @param request
	 * @return
	 */
	public static String processRequest(HttpServletRequest request) {
		String respXml = null;
		// default response
		String respContent = "unknown request type.";
		try {
			// xml请求解析
			Map<String, String> requestMap = MessageUtil.parseXml(request);

			// 发送方帐号（open_id）
			String fromUserName = requestMap.get("FromUserName");
			// 公众帐号
			String toUserName = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");

			// 回复文本消息
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {				
				String textContent = requestMap.get("Content").trim();
				//log.info(textContent);
				
				if("今天".equals(textContent)||"today".equals(textContent)){
					respContent=todayInHistory();
            	}else if("email".equals(textContent)){
            		respContent=sendMail();          		
            	}
				else{
            		respContent = "您发送的是文字信息："+textContent;  
            	}
			}
			// 图片消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "您发送的是图片消息！";
			}
			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				String lat=requestMap.get("Location_X");
				String lng=requestMap.get("Location_Y");
				respContent = "您发送的是地理位置消息！\n"+lat+"\n"+lng;
			}
			// 链接消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "您发送的是链接消息！";
			}
			// 音频消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "您发送的是音频消息！";
			}
			// video
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
				respContent = "video type";
			}
			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = "谢谢您的关注！";
				}
				// 取消订阅
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
				}
				// 自定义菜单点击事件
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// TODO 自定义菜单
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
					// TODO 自定义菜单
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
					// TODO 自定义菜单
				}
			}

			textMessage.setContent(respContent);
			respXml = MessageUtil.textMessageToXml(textMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return respXml;
	}
	
	private static String todayInHistory(){
		String result=null;
		String cur_date = TodayInHistoryService.getMonthDay(0); 
		log.info("current date: "+cur_date);
		
		MySqlUtil mysqlUtil=new MySqlUtil();
		String sql=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		try{						
			Connection conn=mysqlUtil.getConnection();		
			
			sql="select * from today_in_history where it_date=?";					
			ps=conn.prepareStatement(sql);
			ps.setString(1, cur_date);
			rs=ps.executeQuery();
			if(rs.next()){
				result=rs.getString("description");
			}else{						
				result=TodayInHistoryService.getTodayInHistoryInfo();
				sql="insert into today_in_history(it_date,description) values(?,?)";
				ps=conn.prepareStatement(sql);
				ps.setString(1, cur_date);
				ps.setString(2, result);
				int exeResult= ps.executeUpdate();
				if(exeResult==0){ //DML failed
					log.error("insert data- " +cur_date+" failed.");
				}else{	//return the row count for DML statements 
					log.info("insert data- " +cur_date+" successfully.");
				}
			}						
			rs.close();
			ps.close();
			conn.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	private static String sendMail(){
		StopWatch sw=new StopWatch();
		
		ResourceBundle resourceBundle=ResourceBundle.getBundle("mail_conf");
		JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
		senderImpl.setHost(resourceBundle.getString("host"));
		senderImpl.setPort(Integer.parseInt(resourceBundle.getString("port")));
		senderImpl.setUsername(resourceBundle.getString("username")); 
        senderImpl.setPassword(resourceBundle.getString("password")); 
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(resourceBundle.getString("to"));
        mailMessage.setFrom(resourceBundle.getString("from"));
        mailMessage.setSubject(resourceBundle.getString("subject"));
        mailMessage.setText(resourceBundle.getString("text"));
        
        /*Properties prop = new Properties();
        prop.put(" mail.smtp.auth ", " true "); // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
        prop.put(" mail.smtp.timeout ", " 25000 ");
        senderImpl.setJavaMailProperties(prop);*/
        
        senderImpl.send(mailMessage);
        
        log.info("Send mail successfully! "+sw);
        return "Send mail successfully!";
	}
}
