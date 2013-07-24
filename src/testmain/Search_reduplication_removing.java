package testmain;
import java.io.*;
import org.apache.commons.httpclient.*;
import org.htmlparser.util.*;
import crawl.Pages_analysis;
import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;

/**
 * 测试函数 <br/> 对从关键词处理到正文提取的一系列操作进行测试
 * @author Daniel Qian
 * @version 1.0
 */
public class Search_reduplication_removing {

	public static void main(String[] args) throws HttpException, IOException, ParserException, InterruptedException {
		//先调用Search_word_process类处理输入
		Search_word_process searchword = null;
		try {
			searchword = new Search_word_process('X',"数据结构与金融",1);
			searchword.handle_search_word_url();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			//////////应要求重新输入合法的数据
			//////////尚未完成
			e.printStackTrace();
			//若是非法基础搜索引擎代码 则程序已自动更正为B 并自动执行
			//但仍会建议再确认一下基础搜索引擎代码 进行更正并重新执行
			
		}
		
		System.out.println(searchword.getsearch_url());
			
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
//		pages_analysis.analyze_pages(result_links);
		
		//输出result_links链接信息块链表
		result_links.output_all_links();
		
		return ;
	}
	
}