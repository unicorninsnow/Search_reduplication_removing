package datapackage;

import java.util.LinkedList;
import datapackage.Result_Link_Struct;

public class Link_queue {
	private LinkedList<Result_Link_Struct> link_queue_list = new LinkedList<Result_Link_Struct>();
	//若全部完成抓取 则将 is_crawled标志位 置true
	private boolean is_crawled = false;
	
	public void add_link(Result_Link_Struct link){
		link_queue_list.addLast(link);
	}
	public void create_new_link(){
		Result_Link_Struct new_link = new Result_Link_Struct();
		link_queue_list.addLast(new_link);
		return ;
	}
	public void remove_link(){
		link_queue_list.removeFirst();
	}
	public Result_Link_Struct get_last_link(){
		return link_queue_list.getLast();
	}
	public void output_all_links(){
		for(int i = 0;i < link_queue_list.size();++i){
			link_queue_list.get(i).output();
		}
	}
	public boolean get_link_queue_status(){
		//返回 is_crawled标志位 表示link_queue的状态
		return is_crawled;
	}
}
