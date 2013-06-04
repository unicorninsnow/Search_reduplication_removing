package clustered;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.htmlparser.util.*;

import similarityjudge.Similarity_Judgement;


import crawl.Pages_analysis;
import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;
import datapackage.Result_Link_Struct;

public class Clustered {

	ArrayList<Clusteredresult_Queue> list = new ArrayList<Clusteredresult_Queue>();
	public ArrayList<Clusteredresult_Queue> getlist()
	{
		return list;
	}
	public boolean getdb(String keyword,int showpage) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");	
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/searchdb", "search", "search");
		Statement stmt = conn.createStatement();
		String sql = "Select ID from KeywordTable where keyword = '" + keyword +"'";
		ResultSet rs = stmt.executeQuery(sql);
		sql = "Select * from ResultTable where showpage = " + 
				showpage +" and keywordid in (Select ID from KeywordTable where keyword = '" + keyword +"')";
		if(!rs.next())
		{
			return false;
		}
		rs = stmt.executeQuery(sql);
		int n = 1;
		Clusteredresult_Queue queue = new Clusteredresult_Queue();
		if(rs.next())
		{
			do{
				String linktitle = rs.getString("linktitle");
				String linkurl = rs.getString("linkurl");
				String linkabstract = rs.getString("linkabstract");
				int resultnum = Integer.parseInt(rs.getString("resultnum"));
				if(resultnum != n)
				{
					list.add(queue);
					queue = new Clusteredresult_Queue();
					n++;
				}
				queue.insert(linkurl, linktitle, linkabstract);
			}while(rs.next());
			if(queue.gethead() != null)//说明最后一个链表有内容
			{
				list.add(queue);
			}
			return true;
		}else
			return false;
	}
	public void process(String keyword,int showpage) throws SQLException, ClassNotFoundException, ParserException, UnsupportedEncodingException
	{
		if(getdb(keyword, showpage))
			return;
		ArrayList<String> strlist = new ArrayList<String>();
		Clusteredresult_Queue queue;
		for(int i = (showpage-1) * 5 + 1;i <= showpage * 5;i++)
		{
			//先调用Search_word_process类处理输入
			Search_word_process searchword = new Search_word_process();
			searchword.putin('B',keyword,i);
			searchword.choose_engine_search_word();
			searchword.access_to_appointed_page();
		
			//调用Search_engine_process类将结果页面中的有效信息抓取出来
			Search_engine_process search_engine_process = new Search_engine_process();
			search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getNoofpagetoaccess());
		
			//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
			//是否这样处理有待商榷
			Link_queue result_links = search_engine_process.getresult_links();
		
			//调用Pages_analysis类对各个链接进行正文提取
			Pages_analysis pages_analysis = new Pages_analysis();
			pages_analysis.analyze_pages(result_links);
			
			for(int j = 0;j < 10;j++)
			{
				Result_Link_Struct res = result_links.get_link(j);
				String title = res.getLink_title().toString();
				String url = res.getLink_url().toString();
				String abs = res.getLink_abstract().toString();
				String text = res.getLink_text().toString();
				int size = strlist.size(),k;
				for(k = 0;k < size;k++)
				{
					if(Similarity_Judgement.similarity_judge(text,strlist.get(k)))
						break;
				}
				if(k == size)//没有近似的
				{
					strlist.add(text);
					queue =  new Clusteredresult_Queue();
					queue.insert(url, title, abs);
					list.add(queue);
				}else {//已有近似的
					list.get(k).insert(url, title, abs);
				}
			}
			
		}
	}/*
	public void putinlist(String keyword) throws IOException//测试用的谷歌api
	{
	 	String a; 
		a = clustered.Getpage.getPage(keyword);
	 	a = a.substring(a.indexOf("\"items\": ") + 1);
	 	
	 	File file = new File("C://temp.txt");
	 	String data;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
		StringBuilder b = new StringBuilder();
		while((data = br.readLine())!=null)  
		{  
			 b.append(data);
		}
		a = b.substring(b.indexOf("\"items\": ") + 1);
		
		for(int i = 0;i < 5;i++)
		{
			Clusteredresult_Queue queue = new Clusteredresult_Queue();
			for(int j = 0;j < 2;j++)
			{
				String ur,ti,ab;
				a = a.substring(a.indexOf("\"title\": ") + 1);
				ti = a.split("\"",4)[2];
				a = a.substring(a.indexOf("\"link\": ") + 1);
				ur = a.split("\"",4)[2];
				a = a.substring(a.indexOf("\"snippet\": ") + 1);
				ab = a.split("\"",4)[2];
				queue.insert(ur, ti, ab);
			}
			list.add(queue);
		}
	}*/
	public void main(String[] args) throws IOException, SQLException, ClassNotFoundException, ParserException {
		process("a", 1);
	}
}
