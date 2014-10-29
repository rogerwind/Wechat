package com.roger.kxmoment.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.roger.kxmoment.service.CoreService;
import com.roger.kxmoment.util.SignUtil;
import com.roger.kxmoment.util.StopWatch;

/**
 * @author yeyo
 * @date 2014-10-27
 */

public class CoreServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CoreServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		StopWatch sw = new StopWatch();
		log.info("do get begin..");
		PrintWriter out = response.getWriter();
		
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		// 随机字符串
		String echostr = request.getParameter("echostr");

		
		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		if (SignUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}
		out.close();
		out = null;
		log.info("do get end.."+sw);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		StopWatch sw = new StopWatch();
		log.info("do post begin..");
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		PrintWriter out=response.getWriter();
		
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		
		
		if (SignUtil.checkSignature(signature, timestamp, nonce)) {
			String respXML=CoreService.processRequest(request);
			out.print(respXML);
		}
		
		out.close();
		out=null;
		log.info("do post end.."+sw);
	}

}
