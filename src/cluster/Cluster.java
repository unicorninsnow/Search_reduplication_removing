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
	public int getdb(String keyword,int showpage) throws SQLException, ClassNotFoundException
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
			return 1;//没有找到关键字
		}
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
				queue.insert(linkurl, linktitle, linkabstract);
			}while(rs.next());
			if(queue.gethead() != null)//说明最后一个链表有内容
			{
				list.add(queue);
			}
			return 2;//找到并从注册表中提取
		}else
			return 3;//有关键字但没有这页
	}
	public void process(String keyword,int showpage) throws SQLException, ClassNotFoundException, ParserException, IOException
	{
		int flag = getdb(keyword, showpage);
		if(flag == 2)
			return;
		//ArrayList<String> strlist = new ArrayList<String>();
		Clusteredresult_Queue queue;
		
		for(int i = (showpage-1) * 2 + 1;i <= showpage * 2;i++)
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
			//Pages_analysis pages_analysis = new Pages_analysis();
			//pages_analysis.analyze_pages(result_links);
			
			
			int Length = result_links.num_of_links();
			for(int j = 0;j < Length;j++)
			{
				Result_Link_Struct res = result_links.get_link(j);
				String title = "",url = "",abs = "",text = null;
				try{
				title = res.getLink_title().toString();
				url = res.getLink_url().toString();
				abs = res.getLink_abstract().toString();
				//text = res.getLink_text().toString();
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
					if(Similarity_Judgement.title_judge(title,list.get(k).gethead().gettitle().toString()))
						break;
				}
				if(k == size)//没有近似的
				{
					//strlist.add(text);
					queue =  new Clusteredresult_Queue();
					queue.insert(url, title, abs);
					list.add(queue);
				}else {//已有近似的
					list.get(k).insert(url, title, abs);
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
