package clustered;

import java.io.IOException;
import java.util.ArrayList;


public class Clustered {

	ArrayList<Clusteredresult_Queue> list = new ArrayList<Clusteredresult_Queue>();
	public ArrayList<Clusteredresult_Queue> getlist()
	{
		return list;
	}
	
	public void putinlist(String url) throws IOException
	{
	 	String a; 
		a = clustered.Getpage.getPage(url);
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
	public void main(String[] args) throws IOException {
		putinlist("a");
	}
}
