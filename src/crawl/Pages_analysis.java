package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Link_queue;
import datapackage.Result_Link_Struct;


/**
 * 对链接信息块队列中的所有链接进行访问并抓取相关信息<br/>
 * 包括 正文抓取 更新URL 更新标题等
 * @author Daniel Qian
 * @version 1.1 对线程的管理策略改为 抓取线程并发运行 主线程被阻塞至所以线程结束
 *
 */
public class Pages_analysis {
	@Deprecated final int text_para_threshold = 35;

	/**
	 * 对 链接访问抓取的线程队列 进行管理的函数<br/>
	 * 管理策略为 抓取线程并发运行 主线程被阻塞至所以线程结束
	 * @param result_links 链接信息块队列
	 */
	public void analyze_pages_use_thread(Link_queue result_links){
		//为每一个链接信息块创建一个线程
		Thread  []page = new Thread[result_links.num_of_links()];
		//对每个线程进行信息块参数传入 并开始并发运行线程
		for(int i = 0;i < result_links.num_of_links();++i){
			page[i] = new Thread(new Page_thread(result_links.get_link(i)));
			page[i].start();
		}
		//如果全部完成 则标志位为true
		boolean is_all_thread_finished = false;
		while (!is_all_thread_finished) {
			//若没有全部完成 则主线程被阻塞在while循环中
			is_all_thread_finished = true;
			for (int i = 0; i < result_links.num_of_links(); ++i) {
				//只要有活着的线程 即没有完成的  则(!page[i].isAlive()) == false
				//则is_all_thread_finished == false
				is_all_thread_finished = is_all_thread_finished && (!page[i].isAlive());
			}
		}
	}
	
	/**
	 * 用于测试的线程管理函数 <br/>
	 * 与正式执行的管理函数的区别是其没有进行多线程并发 而是顺序执行
	 * @param result_links
	 * @throws InterruptedException 
	 */
	public void analyze_pages(Link_queue result_links) throws InterruptedException{
		//为每一个链接信息块创建一个线程
				Thread  []page = new Thread[result_links.num_of_links()];
				//对每个线程进行信息块参数传入 并开始顺序运行线程
				int i = 2;
//				for(int i = 0;i < result_links.num_of_links();++i){
					page[i] = new Thread(new Page_thread(result_links.get_link(i)));
					page[i].start();
					page[i].join();
//				}
			
		
		return;
	}
	
	@Deprecated 
	public String get_page_title(String url_to_analyze) {
		String title_analyzed = "";
		try {
			Parser parser = new Parser(url_to_analyze);
			NodeFilter title_filter = new TagNameFilter("title");
			
			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			// parser.setEncoding(str1);
			try {
				NodeList titles = parser.extractAllNodesThatMatch(title_filter);
				if (titles != null) {
					if (titles.size() == 1) {
						Node title_text = (Node) titles.elementAt(0);
						title_analyzed = title_text.toPlainTextString();
						System.out.println(title_analyzed);
						System.out.println("=================heihei=============");
					} else {
						System.out.println("why is title not exclusive?");
						System.out.println("=================heihei=============");
					}
				}
			} catch (EncodingChangeException e) {
				String encode = e.toString().split("to ")[1];
				encode = encode.split(" at")[0];
				//System.out.println(encode);
				parser.setEncoding(encode);
				NodeList titles = parser.extractAllNodesThatMatch(title_filter);
				if (titles != null) {
					if (titles.size() == 1) {
						Node title_text = (Node) titles.elementAt(0);
						title_analyzed = title_text.toPlainTextString();
						System.out.println(title_analyzed);
						System.out.println("=================heihei=============");
					} else {
						System.out.println("why is title not exclusive?");
						System.out.println("=================heihei=============");
					}
				}
			}

		} catch (ParserException e) {
			e.printStackTrace();
		}
		return title_analyzed;
	}

	@Deprecated 
	public void get_page_mainbody(Result_Link_Struct page_to_analyze) {
		String mainbody_analyzed = "";
		// System.out.println(page_to_analyze.getLink_title());
		// System.out.println(page_to_analyze.getLink_url());
		try {
			Parser parser = new Parser(page_to_analyze.getLink_url());

			NodeFilter mainbody_filter = new TagNameFilter("p");
			// NodeFilter mainbody_filter_p = new TagNameFilter("p");
			// NodeFilter mainbody_filter_article = new
			// HasAttributeFilter("class", "article");
			// NodeFilter mainbody_filter = new
			// OrFilter(mainbody_filter_p,mainbody_filter_article);
			try {
				NodeList main_body = parser
						.extractAllNodesThatMatch(mainbody_filter);

				if (main_body != null) {
					// System.out.println("main_body.size ===== " +
					// main_body.size());
					for (int i = 0; i < main_body.size(); ++i) {
						Node mainbody_text = (Node) main_body.elementAt(i);
						String temptext = mainbody_text.toPlainTextString()
								.replace("&ldquo;", "“")
								.replace("&rdquo;", "”")
								.replace("&middot;", "·")
								.replace("&nbsp", " ")
								.replace(" ", "").replace("	", "");
						if (temptext.length() > text_para_threshold) {
							mainbody_analyzed = mainbody_analyzed + temptext + "\n";
							// System.out.println(temptext);
						}
						// System.out.println("=================paragraph=============");
					}
					// System.out.println(mainbody_analyzed);
					// System.out.println("=================one page is finished.==================");
					page_to_analyze.setLink_text(mainbody_analyzed);
				} else {
					System.out.println("why is title not exclusive?");
				}
				page_to_analyze.setLink_url(parser.getURL());
			} catch (EncodingChangeException e) {
				String encode = e.toString().split("to ")[1];
				encode = encode.split(" at")[0];
				// System.out.println(encode);
				parser.setEncoding(encode);
				NodeList main_body = parser
						.extractAllNodesThatMatch(mainbody_filter);

				if (main_body != null) {
					// System.out.println("main_body.size ===== " +
					// main_body.size());
					for (int i = 0; i < main_body.size(); ++i) {
						Node mainbody_text = (Node) main_body.elementAt(i);
						String temptext = mainbody_text.toPlainTextString()
								.replace("&ldquo;", "“")
								.replace("&rdquo;", "”")
								.replace("&middot;", "·")
								.replace("&nbsp", " ")
								.replace(" ", "").replace("	", "");
						if (temptext.length() > text_para_threshold) {
							mainbody_analyzed = mainbody_analyzed + temptext + "\n";
							// System.out.println(temptext);
						}
						// System.out.println("=================paragraph=============");
					}
					// System.out.println(mainbody_analyzed);
					// System.out.println("=================one page is finished.==================");
					page_to_analyze.setLink_text(mainbody_analyzed);
				} else {
					System.out.println("why is title not exclusive?");
				}
				page_to_analyze.setLink_url(parser.getURL());
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return;
	}

	@Deprecated
	public void update_page_url(Result_Link_Struct page_to_analyze){
		
	}

}
