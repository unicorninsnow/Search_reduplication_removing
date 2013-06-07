package testmain;
import java.io.*;
import org.apache.commons.httpclient.*;
import org.htmlparser.util.*;
import similarity_judge.Similarity_Judgement;

import crawl.Pages_analysis;
import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;

public class Search_reduplication_removing {

	public static void main(String[] args) throws HttpException, IOException, ParserException {
		//先调用Search_word_process类处理输入
		Search_word_process searchword = new Search_word_process();
		searchword.putin('B',"测试",1);
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
		//result_links.output_all_links();
		
		//判断各个链接间是否共指
		Similarity_Judgement similarity_Judgement = new Similarity_Judgement();
		for(int i = 0;i < result_links.num_of_links() - 1;++i){
			for(int j = i + 1;j < result_links.num_of_links();++j){
				String texti = result_links.get_link(i).getLink_text();
				String titlei = result_links.get_link(i).getLink_title();
				String textj = result_links.get_link(j).getLink_text();
				String titlej = result_links.get_link(j).getLink_title();
				if(similarity_Judgement.similarity_judge(texti,textj, 1)){
					//System.out.println(result_links.get_link(i).getLink_title());
					System.out.println(titlei);
					System.out.println(result_links.get_link(i).getLink_url());
					System.out.println("　与　");
					//System.out.println(result_links.get_link(j).getLink_title());
					System.out.println(titlej);
					System.out.println(result_links.get_link(j).getLink_url());
					System.out.println("共指.");
					System.out.println("=========================================");
				}
				else
				{/*
					System.out.println(titlei);
					System.out.println(result_links.get_link(i).getLink_url());
					System.out.println("　与　");
					//System.out.println(result_links.get_link(j).getLink_title());
					System.out.println(titlej);
					System.out.println(result_links.get_link(j).getLink_url());
					System.out.println("不共指.");
					System.out.println("=========================================");
					*/
				}
			}
		}
		
		
		
		
		return ;
	}
	
}