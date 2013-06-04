package clustered;

import java.io.IOException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;

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
	public void process(String keyword,int showpage) throws SQLException, ClassNotFoundException
	{
		if(getdb(keyword, showpage))
			return;
		
	}
	public void putinlist(String keyword) throws IOException
	{
	 	String a; 
		a = clustered.Getpage.getPage(keyword);
	 	a = a.substring(a.indexOf("\"items\": ") + 1);
	 	/*
	 	File file = new File("C://temp.txt");
	 	String data;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
		StringBuilder b = new StringBuilder();
		while((data = br.readLine())!=null)  
		{  
			 b.append(data);
		}
		a = b.substring(b.indexOf("\"items\": ") + 1);
		*/
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
	}
	public void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		if(getdb("a", 1))
			System.out.println("yes");
		else
			System.out.println("no");
	}
}
