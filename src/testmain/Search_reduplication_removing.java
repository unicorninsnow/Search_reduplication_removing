package testmain;
import java.io.*;
import org.apache.commons.httpclient.*;
import org.htmlparser.util.*;
import crawl.Pages_analysis;
import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;

public class Search_reduplication_removing {

	public static void main(String[] args) throws HttpException, IOException, ParserException {
		//先调用Search_word_process类处理输入
		Search_word_process searchword = new Search_word_process();
		searchword.putin('B',"新时代的机器学习",2);
		searchword.choose_engine_search_word();
		searchword.access_to_appointed_page();
		
		//调用Search_engine_process类将结果页面中的有效信息抓取出来
		Search_engine_process search_engine_process = new Search_engine_process();
		search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getNoofpagetoaccess());
		
		//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
		//是否这样处理有待商榷
		Link_queue result_links = search_engine_process.getresult_links();
		
		//调用Pages_analysis类对各个链接进行正文提取
		Pages_analysis pages_analysis = new Pages_analysis();
		pages_analysis.analyze_pages(result_links);
		
		//输出result_links链接信息块链表
		result_links.output_all_links();
		
		return ;
	}
	
}