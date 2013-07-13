package datapackage;

import java.util.LinkedList;
import datapackage.Result_Link_Struct;

/** 
 * 是表示一组链接信息块的队列 有顺序<br/>
 * 用以存储某个关键词在某个搜索引擎的某一结果页中 所提取的数个链接信息块
 * @author Daniel Qian
 * @version 1.0
 */
//////////能否从LinkedList继承？
public class Link_queue {
/*
 * 类的数据结构
 */
	/**
	 * 类Link_queue中的核心数据结构<br/>
	 * 将LinkedList数据结构用Result_Link_Struct类型实例化<br/>
	 * 暂无个数上限  但典型值为10n
	 */
	private LinkedList<Result_Link_Struct> link_queue_list = new LinkedList<Result_Link_Struct>();
	/** 
	 * 用以表示所有抓取操作是否完成的标志位<br/>
	 * 若全部完成抓取 则 is_crawled == true
	 */
	private boolean is_crawled = false;

/*
 * 类的各种方法函数
 */
	/** 
	 * 将链接信息块link 添加到队列末端
	 * @param link
	 */
	public void add_link(Result_Link_Struct link){
		link_queue_list.addLast(link);
	}
	
	/**
	 * 在队列末端添加一个空白的链接信息块
	 */
	public void create_new_link(){
		Result_Link_Struct new_link = new Result_Link_Struct();
		link_queue_list.addLast(new_link);
		return ;
	}
	
	/**
	 * 移除在链接信息块队列中的 第一个链接信息块
	 */
	public void remove_link(){
		link_queue_list.removeFirst();
	}
	
	/**
	 * 返回该链接信息块队列中的 最后一个链接信息块
	 * @return Result_Link_Struct
	 */
	public Result_Link_Struct get_last_link(){
		return link_queue_list.getLast();
	}
	
	/**
	 * 返回该链接信息块队列中的 第一个链接信息块
	 * @return Result_Link_Struct
	 */
	public Result_Link_Struct get_first_link(){
		return link_queue_list.getFirst();
	}
	
	/**
	 * 返回当前队列的大小 即有多少个链接信息块
	 * @return int
	 */
	public int num_of_links(){
		return link_queue_list.size();
	}
	
	/**
	 *  返回队列中第x个链接信息块
	 * @param x (0<= x && x < size())
	 * @return Result_Link_Struct
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 */
	public Result_Link_Struct get_link(int x){
		return link_queue_list.get(x);
	}
	
	/**
	 * 按链接信息块输出的模式 输出整个链接信息块队列
	 */
	public void output_all_links(){
		for(int i = 0;i < link_queue_list.size();++i){
			link_queue_list.get(i).output();
		}
	}
	
	/**
	 * 返回 is_crawled标志位 表示link_queue的状态
	 * @return boolean
	 */
	public boolean get_link_queue_status(){
		return is_crawled;
	}
}
