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
	 * 处理微信发来的请求
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
			// xml请求解析
			// Map<String, String> requestMap = MessageUtil.parseXml(request);
			Element requestObject = MessageUtil.parseXml(request);
			// 发送方帐号（open_id）
			String fromUserName = requestObject.element("FromUserName")
					.getText();
			// 公众帐号
			String toUserName = requestObject.element("ToUserName").getText();
			// 消息类型
			String msgType = requestObject.element("MsgType").getText();

			// 回复文本消息
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

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String textContent = requestObject.element("Content").getText()
						.trim();

				// log.info(textContent);

				if ("今天".equals(textContent) || "today".equals(textContent)) {
					respContent = todayInHistory();
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if ("email".equals(textContent)) {
					respContent = sendMail();
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if ("2048".equals(textContent)) {

					Article article = new Article();
					article.setTitle("2048游戏");
					article.setDescription("游戏规则很简单，每次可以选择上下左右其中一个方向去滑动，每滑动一次，所有的数字方块都会往滑动的方向靠拢外，系统也会在空白的地方乱数出现一个数字方块，相同数字的方块在靠拢、相撞时会相加。系统给予的数字方块不是2就是4，玩家要想办法在这小小的16格范围中凑出“2048”这个数字方块");
					article.setPicUrl("http://kaixinmoment.duapp.com/2048/1395908994962.png");
					article.setUrl("http://kaixinmoment.duapp.com/2048");
					articleList.add(article);
					// 设置图文消息个数
					newsMessage.setArticleCount(articleList.size());
					// 设置图文消息包含的图文集合
					newsMessage.setArticles(articleList);
					// 将图文消息对象转换成xml字符串
					respXml = MessageUtil.newsMessageToXml(newsMessage);
				} else {
					respContent = "您发送的是文字信息：" + textContent;
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				}

			}
			// 图片消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "您发送的是图片消息！";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				String lat = requestObject.element("Location_X").getText();
				String lng = requestObject.element("Location_Y").getText();
				respContent = "您发送的是地理位置消息！\n" + lat + "\n" + lng;
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// 链接消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "您发送的是链接消息！";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// 音频消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "您发送的是音频消息！";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// video
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
				respContent = "video type";
				textMessage.setContent(respContent);
				respXml = MessageUtil.textMessageToXml(textMessage);
			}
			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {

				String eventType = requestObject.element("Event").getText();

				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = "谢谢您的关注！";
					textMessage.setContent(respContent);
					respXml = MessageUtil.textMessageToXml(textMessage);
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
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
					} else if (eventKey.equals("B2")) {// 周边搜索
						respContent = "周边搜索";
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					} else if (eventKey.equals("B3")) {// 2048

						Article article = new Article();
						article.setTitle("2048游戏");
						article.setDescription("游戏规则很简单，每次可以选择上下左右其中一个方向去滑动，每滑动一次，所有的数字方块都会往滑动的方向靠拢外，系统也会在空白的地方乱数出现一个数字方块，相同数字的方块在靠拢、相撞时会相加。系统给予的数字方块不是2就是4，玩家要想办法在这小小的16格范围中凑出“2048”这个数字方块");
						article.setPicUrl("http://kaixinmoment.duapp.com/2048/1395908994962.png");
						article.setUrl("http://kaixinmoment.duapp.com/2048");
						articleList.add(article);
						// 设置图文消息个数
						newsMessage.setArticleCount(articleList.size());
						// 设置图文消息包含的图文集合
						newsMessage.setArticles(articleList);
						// 将图文消息对象转换成xml字符串
						respXml = MessageUtil.newsMessageToXml(newsMessage);
					}
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN_TEXT)) { // 扫码回内容
					String eventKey = requestObject.element("EventKey")
							.getText();
					if (eventKey.equals("C1")) {// 扫码回内容
						respContent = requestObject.element("ScanCodeInfo")
								.element("ScanResult").getText();
						textMessage.setContent(respContent);
						respXml = MessageUtil.textMessageToXml(textMessage);
					}
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN_URL)) {// 扫码并跳转
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
		 * " true "); // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
		 * prop.put(" mail.smtp.timeout ", " 25000 ");
		 * senderImpl.setJavaMailProperties(prop);
		 */

		senderImpl.send(mailMessage);

		log.info("Send mail successfully! " + sw);
		return "Send mail successfully!";
	}
}
