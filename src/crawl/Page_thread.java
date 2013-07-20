package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Result_Link_Struct;

/**
 * 该类是用于一个链接对应页面的信息抓取的一个线程类<br/>
 * 抓取的信息包括 该链接的URL更新 和 正文抓取
 * @author Daniel Qian
 * @version 1.1 正文抓取采用 连续纯文本阈值法
 */
public class Page_thread implements Runnable {
	/*
	 * 常量变量设定
	 */
	/**
	 * 在 按连续纯文本字数数量 来进行正文识别抓取 的模式中<br/>
	 * 表示连续纯文本字数阈值的常量
	 */
	final int text_para_threshold = 35;
	/** 该Page_thread中进行抓取的页面的信息块 */
	Result_Link_Struct page_to_analyze;

	/*
	 * 构造初始值
	 */
	public Page_thread(Result_Link_Struct page_tobe_analyze) {
		// 通过该类的构造函数 对该Page_thread中进行抓取的页面的信息块 进行从外部向内的参数传递
		page_to_analyze = page_tobe_analyze;
	}

	/*
	 * 线程主方法 默认创建线程后自动运行run()
	 */
	@Override
	public void run() {
		// TODO 自动生成的方法存根
		try {
			crawl_page();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			//////////如何处理抓取正文中的一系列异常？
		}
	}

	/*
	 * 该线程中实际操作的各个函数及功能
	 */

	/**
	 * 抓取某链接页面的信息 包括正文和更新URL<br/>
	 * 采取差错检测等手段提高获取信息的稳定性<br/>
	 * 差错检测包括 编码问题
	 * @throws Exception
	 */
	public void crawl_page() throws Exception {
		
		// System.out.println(page_to_analyze.getLink_title());
		// System.out.println(page_to_analyze.getLink_url());
		try {
			Parser parser = new Parser(page_to_analyze.getLink_url());
			
			// 设定代理
			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
			// 更新链接URL
			page_to_analyze.setLink_url(parser.getURL());
			
			// 设定标题过滤器
			NodeFilter title_filter = new TagNameFilter("title");
			// 设定正文过滤器
			NodeFilter mainbody_filter = new TagNameFilter("p");
			/*
			 * NodeFilter mainbody_filter_p = new TagNameFilter("p"); NodeFilter
			 * mainbody_filter_article = new HasAttributeFilter("class",
			 * "article"); NodeFilter mainbody_filter = new
			 * OrFilter(mainbody_filter_p,mainbody_filter_article);
			 */
			
			try {
				// 进行标题更新
				update_title(parser, title_filter);
				// 进行正文抓取
				get_mainbody(parser, mainbody_filter);
				
			} catch (EncodingChangeException e) {
				/* 对编码有问题的链接进行编码修复并进行第二次抓取 */
				String encode = e.toString().split("to ")[1];
				encode = encode.split(" at")[0];
				// 重设编码
				parser.setEncoding(encode);
				parser.reset();
				
				// 进行标题更新
//				update_title(parser, title_filter);
				// 进行正文抓取
				get_mainbody(parser, mainbody_filter);
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * 实际进行正文抓取的核心函数
	 * @version 1.0 连续纯文本阈值法
	 * @param parser 该链接的解析器
	 * @param mainbody_filter 页面正文的过滤方式
	 * @param mainbody_analyzed 存放正文的字符串
	 * @throws ParserException 解析器的异常
	 * @throws Exception 正文为空异常
	 */
	public void get_mainbody(Parser parser, NodeFilter mainbody_filter) throws ParserException, Exception {
		String mainbody_analyzed = "";
		NodeList main_body = parser.extractAllNodesThatMatch(mainbody_filter);
		// 对 通过该过滤方式无法获取任何正文结点 的情况 抛出空正文异常
		if (main_body == null)
			throw new Exception("Main body is null!");

		for (int i = 0; i < main_body.size(); ++i) {
			Node mainbody_text = (Node) main_body.elementAt(i);
			String temptext = mainbody_text.toPlainTextString()
					.replace("&ldquo;", "“").replace("&rdquo;", "”")
					.replace("&middot;", "·").replace("&nbsp", " ")
					.replace(" ", "").replace("	", "");
			if (temptext.length() > text_para_threshold) {
				mainbody_analyzed = mainbody_analyzed + temptext + "\n";
				// System.out.println(temptext);
			}
			// System.out.println("=================paragraph=============");
		}
		// System.out.println(mainbody_analyzed);
		// System.out.println("=================one page is finished.==================");

		// 将正文内容 存入链接信息块
		page_to_analyze.setLink_text(mainbody_analyzed);
		
		// 对解析器进行重置 以便后续使用parser进行解析不会出错
		parser.reset();
		return;
	}
	
	/**
	 * 对链接的标题进行更新的函数
	 * @param parser 该链接的解析器
	 * @param title_filter 标题的过滤方式
	 * @throws ParserException 解析器的异常
	 */
	public void update_title(Parser parser, NodeFilter title_filter) throws ParserException {
		String title_analyzed = "";
		NodeList titles = parser.extractAllNodesThatMatch(title_filter);
		try {
			// 对 通过该过滤方式无法获取任何正文结点 的情况 抛出空正文异常
			if (titles == null)
				throw new Exception("Title is null!");
			if (titles.size() != 1){
				throw new Exception("Title is not exclusive! Size = " + titles.size());
			}
			// 获取该链接标题
			Node title_text = (Node) titles.elementAt(0);
			title_analyzed = title_text.toPlainTextString();

			// 更新链接标题
			page_to_analyze.setLink_title(title_analyzed);
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			System.out.println(page_to_analyze.getLink_title() + page_to_analyze.getLink_url() + 
					"There're some errors in title updating. Title will not be updated.");
			e.printStackTrace();
		}
		
		// 对解析器进行重置 以便后续使用parser进行解析不会出错
		parser.reset();
		return;
	}

}