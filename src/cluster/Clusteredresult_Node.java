package cluster;
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
	public String getabs()
	{
		return abs;
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

