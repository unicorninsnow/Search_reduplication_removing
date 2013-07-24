package cluster;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.htmlparser.util.*;

import similarity_judge.Similarity_Judgement;


//import crawl.Pages_analysis;
import crawl.Pages_analysis;
import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;
import datapackage.Result_Link_Struct;

public class Cluster {

	ArrayList<Clusteredresult_Queue> list = new ArrayList<Clusteredresult_Queue>();
	public ArrayList<Clusteredresult_Queue> getlist()
	{
		return list;
	}
	/*判断数据是否存在的同时取数据*/
	public int getdb(String keyword,int showpage) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");	
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/searchdb", "search", "search");
		Statement stmt = conn.createStatement();
		String sql = "Select ID from KeywordTable where keyword = '" + keyword +"'";
		ResultSet rs = stmt.executeQuery(sql);
		if(!rs.next())
		{
			return 1;//没有找到关键字
		}
		sql = "Select * from ResultTable where showpage = " + 
				showpage +" and keywordid in (Select ID from KeywordTable where keyword = '" + keyword +"')";
		rs = stmt.executeQuery(sql);
		int n = 0;
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
				queue.insert(linkurl, linktitle, linkabstract, 0);
			}while(rs.next());
			if(queue.gethead() != null)//说明最后一个链表有内容
			{
				list.add(queue);
			}
			return 2;//找到并从注册表中提取
		}else
			return 3;//有关键字但没有这页
	}
	public boolean process_simple(String keyword,int showpage) throws SQLException, ClassNotFoundException
	{//第一次搜索，由于需要抓取内容速度很慢，于是采用比较标题和摘要的方式处理，而后台自动处理抓取部分
		//System.out.println("Start Searching...");
		int flag = getdb(keyword, showpage);
		if(flag == 2)
			return true;//数据库中有数据，正常返回
		//ArrayList<String> strlist = new ArrayList<String>();
		Clusteredresult_Queue queue;
		
		for(int i = (showpage-1) * 2 + 1;i <= showpage * 2;i++)
		{
			//先调用Search_word_process类处理输入
			Search_word_process searchword = null;
			try {
				searchword = new Search_word_process('B',keyword,i);
				searchword.handle_search_word_url();
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				//////////应要求重新输入合法的数据
				//////////尚未完成
				e.printStackTrace();
				//若是非法基础搜索引擎代码 则程序已自动更正为B 并自动执行
				//但仍会建议再确认一下基础搜索引擎代码 进行更正并重新执行
				
			}
		
			//调用Search_engine_process类将结果页面中的有效信息抓取出来
			Search_engine_process search_engine_process = new Search_engine_process();
			try {
				search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getsearch_page());
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			
			//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
			//////////是否这样处理有待商榷
			Link_queue result_links = search_engine_process.getresult_links();
			
			int Length = result_links.num_of_links();
			for(int j = 0;j < Length;j++)
			{
				Result_Link_Struct res = result_links.get_link(j);
				String title = "",url = "",abs = "",text = null;
				try{
				title = res.getLink_title().toString();
				url = res.getLink_url().toString();
				abs = res.getLink_abstract().toString();
				text = res.getLink_text().toString();
				}catch(Exception e)
				{
					if(text == null)
						text = "";
				}
				//int size = strlist.size();
				int size = list.size();
				int k;
				for(k = 0;k < size;k++)
				{
					//if(Similarity_Judgement.similarity_judge(text,strlist.get(k),1))
					if(Similarity_Judgement.title_judge(abs,list.get(k).gethead().getabs().toString()))
						break;
				}
				if(k == size)//没有近似的
				{
					//strlist.add(text);
					queue =  new Clusteredresult_Queue();
					queue.insert(url, title, abs, j + (i-1)*10);
					list.add(queue);
				}else {//已有近似的
					list.get(k).insert(url, title, abs, j + (i-1)*10);
				}
			}
		}
		return false;//还需要后台运行
	}
	public boolean process(String keyword,int showpage) throws SQLException, ClassNotFoundException, ParserException, IOException, InterruptedException
	{
		System.out.println("后台运行中...");
		int flag = getdb(keyword, showpage);
		if(flag == 2)
			return true;//数据库中有数据，正常返回
		ArrayList<String> strlist = new ArrayList<String>();
		Clusteredresult_Queue queue;
		
		for(int i = (showpage-1) * 2 + 1;i <= showpage * 2;i++)
		{
			//先调用Search_word_process类处理输入
			Search_word_process searchword = null;
			try {
				searchword = new Search_word_process('B',keyword,i);
				searchword.handle_search_word_url();
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				//////////应要求重新输入合法的数据
				//////////尚未完成
				e.printStackTrace();
				//若是非法基础搜索引擎代码 则程序已自动更正为B 并自动执行
				//但仍会建议再确认一下基础搜索引擎代码 进行更正并重新执行
				
			}
		
			//调用Search_engine_process类将结果页面中的有效信息抓取出来
			Search_engine_process search_engine_process = new Search_engine_process();
			try {
				search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getsearch_page());
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			
			//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
			//////////是否这样处理有待商榷
			Link_queue result_links = search_engine_process.getresult_links();
			
			//调用Pages_analysis类对各个链接进行正文提取
			Pages_analysis pages_analysis = new Pages_analysis();
			pages_analysis.analyze_pages_use_thread(result_links);
			
			//测试用抓取正文的线程管理代码 其顺序执行 无多线程并发
//			pages_analysis.analyze_pages(result_links);
			
			//输出result_links链接信息块链表
			//result_links.output_all_links();
			
			
			int Length = result_links.num_of_links();
			for(int j = 0;j < Length;j++)
			{
				Result_Link_Struct res = result_links.get_link(j);
				String title = "",url = "",abs = "",text = null;
				try{
				title = res.getLink_title().toString();
				url = res.getLink_url().toString();
				abs = res.getLink_abstract().toString();
				text = res.getLink_text().toString();
				}catch(Exception e)
				{
					if(text == null)
						text = "";
				}
				int size = strlist.size();
				int k;
				for(k = 0;k < size;k++)
				{
					if(Similarity_Judgement.similarity_judge(text,strlist.get(k),1))
						break;
				}
				if(k == size)//没有近似的
				{
					strlist.add(text);
					queue =  new Clusteredresult_Queue();
					queue.insert(url, title, abs ,j + (i-1)*10);//这是和process_simple中对应，便于异步刷新
					list.add(queue);
				}else {//已有近似的
					list.get(k).insert(url, title, abs ,j + (i-1)*10);
				}
			}
		}
		int size = list.size();
		Class.forName("com.mysql.jdbc.Driver");	
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/searchdb", "search", "search");
		Statement stmt = conn.createStatement();

		String sql;
		if(flag == 1)//需要在表中添加关键字再添加项
		{
			sql = "Insert into KeywordTable(keyword) values('" + keyword + "')";
			stmt.executeUpdate(sql);
		}
		sql = "Select ID from KeywordTable where keyword = '" + keyword +"'";
		ResultSet rs = stmt.executeQuery(sql);
		rs.next();
		int keywordid = Integer.parseInt(rs.getString("ID"));
		for(int k = 0;k < size;k++)
		{
			queue = list.get(k);
			Clusteredresult_Node p = queue.head;
			while(p != null)
			{
			sql = "Insert into ResultTable(keywordid,linktitle,linkurl,linkabstract,showpage,resultnum) values(" + keywordid +
					",'" + p.gettitle() +"','" + p.geturl() +"','" + p.getabs() +"'," + showpage + "," + k + ")";
			stmt.executeUpdate(sql);
			p = p.next;
			}
		}
		return false;//需要输出
	}
	/*原使用Googleapi处理函数*/
	/*
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
	public void main(String[] args) throws IOException, SQLException, ClassNotFoundException, ParserException, InterruptedException {
		process("a", 1);
	}
}
