package com.roger.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		/*StringBuffer buffer=new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<person>");
		buffer.append("<name>roger</name>");
		buffer.append("<sex>male</sex>");
		buffer.append("<address>sichuan,China</address>");
		buffer.append("</person>");
		
		Document doc=DocumentHelper.parseText(buffer.toString());
		Element root=doc.getRootElement();
		List<Element> elementList=root.elements();
		for(Element e:elementList){
			System.out.println(e.getName()+" => "+e.getText());
		}*/
		/*-------------------*/
		
		//System.out.println(TodayInHistoryService.getTodayInHistoryInfo());
		
		/*-------------------*/
		
		/*String a=null;
		String b="";		
		System.out.println(a);
		System.out.println(b.isEmpty());*/
		
		int i=1;
		String source="<li><a href=\"/news7147/\" title=\"1993年10月28日 “马家军”破世界纪录被全部批准\">1993年10月28日 “马家军”破世界纪录被全部批准</a>&nbsp;&nbsp;</li>   <li><a href=\"/news7148/\" title=\"1995年10月28日 科克伦走钢丝跨越夔门天堑  创造吉尼斯纪录\">1995年10月28日 科克伦走钢丝跨越夔门天堑  创造吉尼斯纪录</a>&nbsp;&nbsp;</li>";
		String[] arr=source.split("   ");
		String dateTag="10月28日";
		for(String e:arr){
			System.out.println(i++);
			System.out.println(e);
			e = e.replace(dateTag, "").replace("&nbsp;&nbsp;", "").replace("（图）", "").replaceAll("</?[^>]+>", "").trim();  
			System.out.println(e);
			
			/*e = e.replace(dateTag, "");
			System.out.println(e);
			e=e.replace("&nbsp;&nbsp;", "");
			System.out.println(e);
			e=e.replace("（图）", "");
			System.out.println(e);
			e=e.replaceAll("</?[^>]+>", "");
			System.out.println(e);
			e=e.trim();
			System.out.println(e);*/
		}
		System.out.println("----------------------");
		Pattern p = Pattern.compile("(.*)(<div class=\"listren\">)(.*?)(</div>)(.*)"); 
		Matcher m = p.matcher("<div class=\"listren\">   <ul>   <li><a href=\"/news7153/\" title=\"1998年10月28日 机长袁斌劫机到台湾\">1998年10月28日 机长袁斌劫机到台湾</a>&nbsp;&nbsp;（图）</li>   <li><a href=\"/news7154/\" title=\"2000年10月28日 中海壳牌八十万吨乙烯工程合营合同签字\">2000年10月28日 中海壳牌八十万吨乙烯工程合营合同签字</a>&nbsp;&nbsp;</li>   <li><a href=\"/news7155/\" title=\"2000年10月28日 巴黎中国文化季隆重开幕\">2000年10月28日 巴黎中国文化季隆重开幕</a>&nbsp;&nbsp;</li>    </ul>   </div>"); 
		if(m.find()){
			System.out.println(m.groupCount());
			
			//String temp=m.group(3);
			//System.out.println(temp);
			 
			for (String info : m.group(3).split("   ")) { 
				info = info.replace(dateTag, "").replace("&nbsp;&nbsp;", "").replace("（图）", "").replaceAll("</?[^>]+>", "").trim(); 
				System.out.println(info);
			}
		}
		
		
	}

}
