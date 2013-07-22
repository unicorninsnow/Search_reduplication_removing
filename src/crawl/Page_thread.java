package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Result_Link_Struct;

/**
 * 该类是用于一个链接对应页面的信息抓取的一个线程类<br/>
 * 抓取的信息包括 该链接的URL更新 和 正文抓取
 * @author Daniel Qian
 * @version 1.2 正文抓取采用 TextTag结点法
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
		try {
			Parser parser = new Parser(page_to_analyze.getLink_url());
			
			// 设定代理
			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
			// 更新链接URL
			page_to_analyze.setLink_url(parser.getURL());
			
			try {
				// 进行标题更新
//				update_title(parser);
				// 进行正文抓取
				get_mainbody_TextTag(parser);	//TextTag法
//				get_mainbody_continuous_text(parser);	//连续纯文本阈值法
				
			} catch (EncodingChangeException e) {
				/* 对编码有问题的链接进行编码修复并进行第二次抓取 */
				String encode = e.toString().split("to ")[1];
				encode = encode.split(" at")[0];
				// 重设编码
				parser.setEncoding(encode);
				parser.reset();
				
				// 进行标题更新
//				update_title(parser);
				// 进行正文抓取
				get_mainbody_TextTag(parser);	//TextTag法
//				get_mainbody_continuous_text(parser);	//连续纯文本阈值法
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * 实际进行正文抓取的核心函数 TextNode标签法
	 * @version 1.1.2 TextNode标签法 除去了一些父结点是ScriptTag或StyleTag 但去除的不彻底<br/>
	 * 已知BUG：对百度文库之类内嵌在swf的正文毫无办法
	 * @param parser 该链接的解析器
	 * @param mainbody_filter 页面正文的过滤方式
	 * @param mainbody_analyzed 存放正文的字符串
	 * @throws ParserException 解析器的异常
	 * @throws Exception 正文为空异常
	 */
	public void get_mainbody_TextTag(Parser parser) throws ParserException, Exception {
		//对可能的正文大结点做过滤
		NodeFilter bodyFilter = new TagNameFilter("body");
		NodeList bodys = parser.extractAllNodesThatMatch(bodyFilter);
		if(bodys.size() != 1)	throw new Exception("no body node!");
		Node body = bodys.elementAt(0);
		
		//设定取得各个正文句段的过滤方式 并据此进行抓取正文
		NodeFilter mainbody_filter = new NodeClassFilter(TextNode.class);
		NodeList main_bodys = new NodeList();
		body.collectInto(main_bodys, mainbody_filter);
		
		String mainbody_analyzed = "";
		
		// 对 通过该过滤方式无法获取任何正文结点 的情况 抛出空正文异常
		if (main_bodys.size() == 0)
			throw new Exception("Main body is null!");

		for (int i = 0; i < main_bodys.size(); ++i) {
			
			Node mainbody_text = (Node) main_bodys.elementAt(i);
			
			//若该TextTag结点其父结点为ScriptTag或是StyleTag 则忽略该TextTag结点
			if((mainbody_text.getParent() instanceof ScriptTag) || (mainbody_text.getParent() instanceof StyleTag)) continue;
			
			//取出正文String
			String temptext = mainbody_text.toPlainTextString();
			//替换一些HTML的转义字符
			temptext = temptext.replace("&ldquo;", "“").replace("&rdquo;", "”")
					.replace("&middot;", "·").replace("&nbsp;", " ")
					.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;","&").replace("&copy;","©" )
					.replace("&quot;", "\"").replace("&apos;", "'");
			temptext = temptext.replace("\n", "").replace("\r", "");
			temptext = temptext.replace("\t", " ");
//			temptext = temptext.replace("　", "").replace(" ", "");
			
				mainbody_analyzed = mainbody_analyzed + temptext;// + "\n";
		}

		// 将正文内容 存入链接信息块
		page_to_analyze.setLink_text(mainbody_analyzed);
		
		// 对解析器进行重置 以便后续使用parser进行解析不会出错
		parser.reset();
		return;
	}
	
	
	/**
	 * 实际进行正文抓取的核心函数 连续纯文本阈值法
	 * @version 1.0 连续纯文本阈值法<br/>
	 * 已知BUG：无法对分句成许多小句(长度小于阈值)的正文结点进行抓取
	 * @param parser 该链接的解析器
	 * @param mainbody_filter 页面正文的过滤方式
	 * @param mainbody_analyzed 存放正文的字符串
	 * @throws ParserException 解析器的异常
	 * @throws Exception 正文为空异常
	 */
	public void get_mainbody_continuous_text(Parser parser) throws ParserException, Exception {
		// 设定正文过滤器
		NodeFilter mainbody_filter = new TagNameFilter("p");
		
		String mainbody_analyzed = "";
		NodeList main_body = parser.extractAllNodesThatMatch(mainbody_filter);
		// 对 通过该过滤方式无法获取任何正文结点 的情况 抛出空正文异常
		if (main_body == null)
			throw new Exception("Main body is null!");

		for (int i = 0; i < main_body.size(); ++i) {
			Node mainbody_text = (Node) main_body.elementAt(i);
			String temptext = mainbody_text.toPlainTextString()
					.replace("&ldquo;", "“").replace("&rdquo;", "”")
					.replace("&middot;", "·").replace("&nbsp;", " ")
					.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;","&")
					.replace("&quot;", "\"").replace(" ", "").replace("	", "");
			if (temptext.length() > text_para_threshold) {
				mainbody_analyzed = mainbody_analyzed + temptext + "\n";
			}
		}

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
	public void update_title(Parser parser) throws ParserException {
		// 设定标题过滤器
		NodeFilter title_filter = new TagNameFilter("title");
		
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