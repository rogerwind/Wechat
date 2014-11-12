package com.roger.kxmoment.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.roger.kxmoment.response.Article;
import com.roger.kxmoment.response.NewsMessage;
import com.roger.kxmoment.response.TextMessage;
import com.roger.kxmoment.util.MessageUtil;
import com.roger.kxmoment.util.MySqlUtil;
import com.roger.kxmoment.util.StopWatch;

public class CoreService
{

	private static Logger log = Logger.getLogger(CoreService.class);

	/**
	 * ����΢�ŷ���������
	 * 
	 * @param request
	 * @return
	 */
	public static String processRequest(HttpServletRequest request)
	{
		String respXml = null;
		// default response
		String respContent = "unknown request type.";
		try {
			// xml�������
			// Map<String, String> requestMap = MessageUtil.parseXml(request);
			Element requestObject = MessageUtil.parseXml(request);
			// ���ͷ��ʺţ�open_id��
			String fromUserName = requestObject.element("FromUserName")
					.getText();
			// �����ʺ�
			String toUserName = requestObject.element("ToUserName").getText();
			// ��Ϣ����
			String msgType = requestObject.element("MsgType").getText();

			// �ظ��ı���Ϣ
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

			NewsMessage newsMessage = new NewsMessage();
			newsMessage.setToUserName(fromUserName);
			newsMessage.setFromUserName(toUserName);
			newsMessage.setCreateTime(new Date().getTime());
			newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);

			List<Article> articleList = new ArrayList<Article>();

			// �ı���Ϣ
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String textContent = requestObject.element("Content").getText()
						.trim();

				// log.info(textContent);

				if ("����".equals(textContent) || "today".equals(textContent)) {
					respContent = todayInHistory();
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if ("email".equals(textContent)) {
					respContent = sendMail();
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if ("2048".equals(textContent)) {

					Article article = new Article();
					article.setTitle("2048��Ϸ");
					article.setDescription("��Ϸ����ܼ򵥣�ÿ�ο���ѡ��������������һ������ȥ������ÿ����һ�Σ����е����ַ��鶼���������ķ���£�⣬ϵͳҲ���ڿհ׵ĵط���������һ�����ַ��飬��ͬ���ֵķ����ڿ�£����ײʱ����ӡ�ϵͳ��������ַ��鲻��2����4�����Ҫ��취����СС��16��Χ�дճ���2048��������ַ���");
					article.setPicUrl("http://kaixinmoment.duapp.com/2048/1395908994962.png");
					article.setUrl("http://kaixinmoment.duapp.com/2048");
					articleList.add(article);
					// ����ͼ����Ϣ����
					newsMessage.setArticleCount(articleList.size());
					// ����ͼ����Ϣ������ͼ�ļ���
					newsMessage.setArticles(articleList);
					// ��ͼ����Ϣ����ת����xml�ַ���
					respXml = MessageUtil.newsMessageToXml(newsMessage);
				} else {
					respContent = "�����͵���������Ϣ��" + textContent;
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				}

			}
			// ͼƬ��Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "�����͵���ͼƬ��Ϣ��";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// ����λ����Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				String lat = requestObject.element("Location_X").getText();
				String lng = requestObject.element("Location_Y").getText();
				respContent = "�����͵��ǵ���λ����Ϣ��\n" + lat + "\n" + lng;
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// ������Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "�����͵���������Ϣ��";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// ��Ƶ��Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "�����͵�����Ƶ��Ϣ��";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// video
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
				respContent = "video type";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// �¼�����
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {

				String eventType = requestObject.element("Event").getText();

				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = "лл���Ĺ�ע��";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO ȡ�����ĺ��û����ղ������ںŷ��͵���Ϣ����˲���Ҫ�ظ���Ϣ
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// respContent="click button";
					String eventKey = requestObject.element("EventKey")
							.getText();

					if (eventKey.equals("A1")) {// today in history
						respContent = todayInHistory();
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					} else if (eventKey.equals("B1")) {// Email Notify
						respContent = sendMail();
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					} else if (eventKey.equals("B2")) {// �ܱ�����
						respContent = "�ܱ�����";
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					} else if (eventKey.equals("B3")) {// 2048

						Article article = new Article();
						article.setTitle("2048��Ϸ");
						article.setDescription("��Ϸ����ܼ򵥣�ÿ�ο���ѡ��������������һ������ȥ������ÿ����һ�Σ����е����ַ��鶼���������ķ���£�⣬ϵͳҲ���ڿհ׵ĵط���������һ�����ַ��飬��ͬ���ֵķ����ڿ�£����ײʱ����ӡ�ϵͳ��������ַ��鲻��2����4�����Ҫ��취����СС��16��Χ�дճ���2048��������ַ���");
						article.setPicUrl("http://kaixinmoment.duapp.com/2048/1395908994962.png");
						article.setUrl("http://kaixinmoment.duapp.com/2048");
						articleList.add(article);
						// ����ͼ����Ϣ����
						newsMessage.setArticleCount(articleList.size());
						// ����ͼ����Ϣ������ͼ�ļ���
						newsMessage.setArticles(articleList);
						// ��ͼ����Ϣ����ת����xml�ַ���
						respXml = MessageUtil.newsMessageToXml(newsMessage);
					}
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN_TEXT)) { // ɨ�������
					String eventKey = requestObject.element("EventKey")
							.getText();
					if (eventKey.equals("C1")) {// ɨ�������
						respContent = requestObject.element("ScanCodeInfo")
								.element("ScanResult").getText();
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					}
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN_URL)) {// ɨ�벢��ת
					respContent = "scan url and redirect.";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
					respContent = "scan qrcode.";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
					respContent = "upload location detail.";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_VIEW)) {
					respContent = "page redirect.";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return respXml;
	}

	private static String todayInHistory()
	{
		String result = null;
		String cur_date = TodayInHistoryService.getMonthDay(0);
		log.info("current date: " + cur_date);

		MySqlUtil mysqlUtil = new MySqlUtil();
		String sql = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = mysqlUtil.getConnection();

			sql = "select * from today_in_history where it_date=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, cur_date);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("description");
			} else {
				result = TodayInHistoryService.getTodayInHistoryInfo();
				sql = "insert into today_in_history(it_date,description) values(?,?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, cur_date);
				ps.setString(2, result);
				int exeResult = ps.executeUpdate();
				if (exeResult == 0) { // DML failed
					log.error("insert data- " + cur_date + " failed.");
				} else { // return the row count for DML statements
					log.info("insert data- " + cur_date + " successfully.");
				}
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String sendMail()
	{
		StopWatch sw = new StopWatch();

		ResourceBundle resourceBundle = ResourceBundle.getBundle("mail_conf");
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

		/*
		 * Properties prop = new Properties(); prop.put(" mail.smtp.auth ",
		 * " true "); // �����������Ϊtrue���÷�����������֤,��֤�û����������Ƿ���ȷ
		 * prop.put(" mail.smtp.timeout ", " 25000 ");
		 * senderImpl.setJavaMailProperties(prop);
		 */

		senderImpl.send(mailMessage);

		log.info("Send mail successfully! " + sw);
		return "Send mail successfully!";
	}
}
