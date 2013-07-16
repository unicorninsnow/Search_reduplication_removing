package testmain;
import java.io.*;
/*
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
*/
import java.util.*;

import org.apache.commons.httpclient.*;
/*
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
*/
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/*
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
*/
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.LinkTag;

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
			searchword = new Search_word_process('B',"人工智能",1);
			searchword.handle_search_word_url();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			//////////应要求重新输入合法的数据
			e.printStackTrace();
		}
			
		//调用Search_engine_process类将结果页面中的有效信息抓取出来
		Search_engine_process search_engine_process = new Search_engine_process();
		try {
//			search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getNoofpagetoaccess());
			search_engine_process.test_extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getNoofpagetoaccess());
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		//建立一个全局的result_links链接信息块链表接收抓取出的各个链接的信息
		//////////是否这样处理有待商榷
		Link_queue result_links = search_engine_process.getresult_links();
		
		//调用Pages_analysis类对各个链接进行正文提取
		Pages_analysis pages_analysis = new Pages_analysis();
//		pages_analysis.analyze_pages_use_thread(result_links);
		
		//测试用抓取正文的线程管理代码 其顺序执行 无多线程并发
//		pages_analysis.analyze_pages(result_links);
		
		
		//输出result_links链接信息块链表
//		result_links.output_all_links();
		
		return ;
	}
	
}