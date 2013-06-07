package cluster;

public class Clusteredresult_Node
{
	String title;
	String url;
	String abs;
	public Clusteredresult_Node(String s1,String s2,String s3)
	{
		url = s2;
		title = s1;
		abs = s3;
	}
	public String geturl()
	{
		return url;
	}
	public String gettitle()
	{
		return title;
	}
	public String getabs()
	{
		return abs;
	}
	
	Clusteredresult_Node next = null;
	
	public Clusteredresult_Node getnext()
	{
		return next;
	}
}

