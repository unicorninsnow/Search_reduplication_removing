package cluster;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*用于显示在网页中的网页链表节点*/
public class Clusteredresult_Node
{
	String title;//标题
	String url;//路径
	String abs;//摘要
	int id;//原顺序
	public Clusteredresult_Node(String s1,String s2,String s3,int s4)
	{
		url = s2;
		title = s1;
		abs = s3;
		id = s4;
	}
	public String geturl()
	{
		return url;
	}
	public String gettitle()
	{
		return title;
	}
	public String gettitle(String key)
	{
		String wordReg = "(?i)" + key;//用(?i)来忽略大小写  
		StringBuffer sb = new StringBuffer();  
		Matcher matcher = Pattern.compile(wordReg).matcher(title);  
		while(matcher.find()){  
			 matcher.appendReplacement(sb, "<font color='red'>"+matcher.group()+"</font>");//这样保证了原文的大小写没有发生变化
		}
		matcher.appendTail(sb);  
		return sb.toString(); 
	}
	public String getabs()
	{
		return abs;
	}
	public String getabs(String key)
	{
		String wordReg = "(?i)" + key;//用(?i)来忽略大小写  
		StringBuffer sb = new StringBuffer();  
		Matcher matcher = Pattern.compile(wordReg).matcher(abs);  
		while(matcher.find()){  
			 matcher.appendReplacement(sb, "<font color='red'>"+matcher.group()+"</font>");//这样保证了原文的大小写没有发生变化
		}
		matcher.appendTail(sb);  
		return sb.toString(); 
	}
	public int getid()
	{
		return id;
	}
	Clusteredresult_Node next = null;
	
	public Clusteredresult_Node getnext()
	{
		return next;
	}
}

