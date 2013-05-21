package clustered;

public class Clusteredresult_Queue
{
	Clusteredresult_Node head = null;	//数组中每个指针代表同类网页结果的链表头指针
	//int length = 0;
	public Clusteredresult_Node gethead()
	{
		return head;
	}
	public void insert(String Url,String Title,String Abs) 
/*在当前结点前插入一个结点，并使其成为当前结点*/ 
	{ 
		Clusteredresult_Node e=new Clusteredresult_Node(Title,Url,Abs); 
	    if(head == null) 
        { 
	    	head = e;
        } 
	    else 
	    {
	    	e.next = head;
	    	head = e;
	    } 
	}
}
