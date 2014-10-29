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
	 * ����΢�ŷ���������
	 * 
	 * @param request
	 * @return
	 */
	public static String processRequest(HttpServletRequest request) {
		String respXml = null;
		// default response
		String respContent = "unknown request type.";
		try {
			// xml�������
			Map<String, String> requestMap = MessageUtil.parseXml(request);

			// ���ͷ��ʺţ�open_id��
			String fromUserName = requestMap.get("FromUserName");
			// �����ʺ�
			String toUserName = requestMap.get("ToUserName");
			// ��Ϣ����
			String msgType = requestMap.get("MsgType");

			// �ظ��ı���Ϣ
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

			// �ı���Ϣ
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {				
				String textContent = requestMap.get("Content").trim();
				//log.info(textContent);
				
				if("����".equals(textContent)||"today".equals(textContent)){
					respContent=todayInHistory();
            	}else if("email".equals(textContent)){
            		respContent=sendMail();          		
            	}
				else{
            		respContent = "�����͵���������Ϣ��"+textContent;  
            	}
			}
			// ͼƬ��Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "�����͵���ͼƬ��Ϣ��";
			}
			// ����λ����Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				String lat=requestMap.get("Location_X");
				String lng=requestMap.get("Location_Y");
				respContent = "�����͵��ǵ���λ����Ϣ��\n"+lat+"\n"+lng;
			}
			// ������Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "�����͵���������Ϣ��";
			}
			// ��Ƶ��Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "�����͵�����Ƶ��Ϣ��";
			}
			// video
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
				respContent = "video type";
			}
			// �¼�����
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// �¼�����
				String eventType = requestMap.get("Event");
				// ����
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = "лл���Ĺ�ע��";
				}
				// ȡ������
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO ȡ�����ĺ��û����ղ������ںŷ��͵���Ϣ����˲���Ҫ�ظ���Ϣ
				}
				// �Զ���˵�����¼�
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// TODO �Զ���˵�
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
					// TODO �Զ���˵�
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
					// TODO �Զ���˵�
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
        prop.put(" mail.smtp.auth ", " true "); // �����������Ϊtrue���÷�����������֤,��֤�û����������Ƿ���ȷ
        prop.put(" mail.smtp.timeout ", " 25000 ");
        senderImpl.setJavaMailProperties(prop);*/
        
        senderImpl.send(mailMessage);
        
        log.info("Send mail successfully! "+sw);
        return "Send mail successfully!";
	}
}
