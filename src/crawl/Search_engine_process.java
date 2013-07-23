package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Result_Link_Struct;
import datapackage.Link_queue;

/**
 * 该类为分析搜索结果页面的类<br/>
 * 将各个结果链接的标题 URL 摘要等信息 抓取到相应的链接信息块队列中
 * 
 * @author Daniel
 * @version 1.1.4 对搜索引擎结果页面的抓取过程进行优化减少异常发生的状况<br/>
 * 已知bug：<br/>
 * 谷歌抓取不稳定 时常会Connection reset 
 */
public class Search_engine_process {
	//////////如何处理Search_engine_process类和Pages_analysis类对result_links链表的共同使用问题
	/**链接信息块队列<br/>典型长度值为10n*/
	Link_queue result_links = new Link_queue();
	
	/**
	 * 返回链接信息块队列 <br/>
	 * 通常在抓取过程完成后进行链接信息块队列的获取
	 * @return Link_queue链接信息块队列
	 */
	public Link_queue getresult_links(){
		return result_links;
	}

	/**
	 * 分类对不同搜索引擎的结果页面进行 各链接信息进行抓取 的函数<br/>
	 * 包括对解析器的一些设置操作
	 * @version 1.3 对抓取过程进行优化<br/> 
	 * 增加对&lt;font size="-1"&gt;类型摘要的支持<br/>
	 * 已知bug：<br/> 
	 * 谷歌抓取不稳定 时常会Connection reset  
	 * @param url (String) 将要被抓取的搜索结果页面的URL
	 * @param search_mode (char) 基础搜索引擎代码
	 * @param search_page (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception 抓取过程异常 主要是parser异常
	 */
	public void extractLinks(String url, char search_mode, int search_page) throws Exception {
		try {
			Parser parser = new Parser();
			parser.setURL(url);

			/*
			 * 对不同的基础搜索引擎所对应的结果页面 分情况进行信息抓取
			 */
			switch (search_mode) {
			case 'B':// 百度
				extractLinks_baidu(parser, search_page);
				break;
			case 'G':// 谷歌
				// 代理设置 用于解决国内网络有时被墙的情况
				 Parser.getConnectionManager().setProxyHost("127.0.0.1");
				 Parser.getConnectionManager().setProxyPort(8118);

				extractLinks_google(parser, search_page);
				break;
			default:
				throw new Exception("Illegal search mode!");
			}

		} catch (ParserException e) {
			e.printStackTrace();

			throw new Exception("Parser Exception!");
		}
		return;
	}
	
	/**
	 * 对百度结果页面进行 各链接的标题 URL 摘要等信息进行抓取 的具体函数
	 * @version 1.1 增加对&lt;font size="-1"&gt;类型摘要的支持
	 * @param parser 页面解析器
	 * @param search_page (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception
	 */
	public void extractLinks_baidu(Parser parser, int search_page) throws Exception {
		/* 设定符合百度的结果链接块过滤方式 并由此对解析器的内容进行过滤 */
		NodeFilter result_filter_regu = new HasAttributeFilter("class", "result");
		NodeFilter result_filter_op = new HasAttributeFilter("class", "result-op");
		OrFilter result_filter = new OrFilter(result_filter_regu, result_filter_op);
		NodeList nodes = parser.extractAllNodesThatMatch(result_filter);

		
		/* 对各个结果链接结点进行抓取过程 */
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); ++i) {
				// 创建一个结果链接存储结构
				Result_Link_Struct result_link_struct = new Result_Link_Struct();

				// 逐个取出符合条件的链接结点
				Node resultNode = (Node) nodes.elementAt(i);

				// 获取链接所在原页号
				result_link_struct.setLink_page_from(search_page);
				// 获取链接原号数
				int result_num_from = Integer.parseInt(((TagNode) resultNode).getAttribute("id"));
				result_link_struct.setLink_num_from(result_num_from);
				
//				if ((((TagNode) resultNode).getAttribute("class")).equals("result")) {
				if(true){
					/* class="result"的正常情况 */

					// 获取结果块的第一个tr结点 trNode 即有效内容的tr结点
					NodeFilter trFilter = new TagNameFilter("tr");
					NodeList trNodeList = new NodeList();
					resultNode.collectInto(trNodeList, trFilter);
					if (trNodeList.size() == 0)
						throw new Exception("no tr tag.errrrrrrrrrrr.");
					Node trNode = trNodeList.elementAt(0);

					/* 处理链接标题和链接URL(百度跳转URL) */
					// 有效的真正链接结点(包含标题和URL信息的最精确的结点)
					LinkTag effective_linktag = null;
					// 用以存储主链接结点是否常规的标志位
					boolean is_mainlink_regular = true;

					// 对主tr结点中有几个<h3 class="t">...</h3>这样的有效连接结点进行分类讨论
					// mianklinkNode即是<h3 class="t">...</h3>结点
					NodeFilter linkclass_t = new HasAttributeFilter("class", "t");
					NodeList mainlinkNodeList = new NodeList();
					trNode.collectInto(mainlinkNodeList, linkclass_t);
					Node mainlinkNode = null;
					if (mainlinkNodeList.size() != 0) {
						// 取第一个<h3 class="t">...</h3>
						mainlinkNode = mainlinkNodeList.elementAt(0);

						// 取出有效的真正链接结点effective_linktag
						// 此处所做的改变是能保证取出的一定是linktag 故不会发生强制类型转换出错的问题
						if (mainlinkNode.getFirstChild() instanceof LinkTag) {
							// 常规情况 能在第一个<h3 class="t">...</h3>的第一个子结点中取出linktag
							effective_linktag = (LinkTag) mainlinkNode.getFirstChild();
						} else {
							// 非常规情况 将是否常规的标志位置false 后转处理非常规的代码
							is_mainlink_regular = false;
						}
					} else {
						// 没有<h3 class="t">...</h3>的情况 为非常规主链接结点的情形
						// 将is_mainlink_regular标志位设为非常规
						is_mainlink_regular = false;
					}

					// 处理非常规的主链接结点的情形
					if (is_mainlink_regular == false) {
						NodeFilter link_Filter = new NodeClassFilter(LinkTag.class);
						NodeList linkList = new NodeList();
						trNode.collectInto(linkList, link_Filter);
						effective_linktag = (LinkTag) linkList.elementAt(0);
					}

					// 存储链接标题
					result_link_struct.setLink_title(effective_linktag.getLinkText());
					// 存储链接URL(此处为百度跳转URL)
					result_link_struct.setLink_url(effective_linktag.getLink());

					/* 处理摘要 */
					NodeFilter cabstractFilter = new HasAttributeFilter("class", "c-abstract");
					NodeList abstractNodeList = new NodeList();
					trNode.collectInto(abstractNodeList, cabstractFilter);
					Node abstractNode = null;
					String link_abstractString;
					if (abstractNodeList.size() != 0) {
						// 处理最简单的摘要模式 直接取第一个c-abstract结点
						abstractNode = abstractNodeList.elementAt(0);
						// 对摘要中可能出现的一些html转义字符作替换
						link_abstractString = abstractNode.toPlainTextString().replace("&ldquo;", "“")
								.replace("&rdquo;", "”").replace("&middot;", "·").replace("&nbsp;", " ")
								.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;","&")
								.replace("&quot;", "\"").replace(" ", "").replace("	", "");
					} else {
						// 处理各种非常规的摘要模式
//						System.out.println("sorry. 第" + result_num_from + "个链接为非常规的摘要模式!");
						//////////尚未完成
						
						//处理<font size="-1">
						NodeFilter fontsize_Filter = new HasAttributeFilter("size","-1");
						NodeList fontsizeList = new NodeList();
						trNode.collectInto(fontsizeList, fontsize_Filter);
						link_abstractString = "";
						if(fontsizeList.size() != 0){
							//最好取出纯的<font size="-1">结点
							int k = 0;
							while(k < fontsizeList.size()){
								if(((TagNode)fontsizeList.elementAt(k)).getAttribute("class") == null){
									abstractNode = fontsizeList.elementAt(k);
									break;
								}
								++k;
							}
							if (k >= fontsizeList.size()) abstractNode = fontsizeList.elementAt(0);
							
							//从fontsize中取摘要结点
							Node p = abstractNode.getNextSibling();
							for(; (!p.getText().equals("/font")); p = p.getNextSibling()){
								//将不需要的结点滤去 
								//////////这里没能将百度快照之前的-去除
								if((p instanceof Span) || (p instanceof ParagraphTag) || (p instanceof LinkTag))		continue;
								//将有效的摘要存入摘要字符串
								link_abstractString = link_abstractString.concat(p.toPlainTextString());
							}
							//对摘要字符串做一些HTML转义字符的替换
							link_abstractString = link_abstractString.replace("&ldquo;", "“")
									.replace("&rdquo;", "”").replace("&middot;", "·").replace("&nbsp;", " ")
									.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;","&")
									.replace("&quot;", "\"").replace(" ", "").replace("	", "");
//							System.out.println("非常规的摘要：" + link_abstractString);
						}else{
							//处理非正常情况的摘要且摘要不在<font size="-1">中情形
							//////////尚未完成 尚未发现该情况的具体实例
						}
						
					}
					result_link_struct.setLink_abstract(link_abstractString);
					
					/* 至此完成百度在正常情况(即<table class="result">情形)下的抓取 */
					
					//////////目前先把<table class="result-op">当成<table class="result">处理
					//////////原因是大多数result-op是集合连接块 到底怎么处理尚未确定
//				}else{
//					//处理class="result-op"的情况
//					//////////尚未完成
//					System.out.println("Not regular! 第" + result_num_from + "个链接为<class=\"result-op\">型");
				}

				/* 将该链接的信息块 顺序存入链接信息块队列中 */
				result_links.add_link(result_link_struct);
			}
		}
		// 对解析器进行重置 以便后续使用parser进行解析不会出错
		parser.reset();
		return;
	}

	/**
	 * 对谷歌结果页面进行 各链接的标题 URL 摘要等信息进行抓取 的具体函数<br/>
	 * 已知bug：<br/> 
	 * 不稳定 时常会Connection reset
	 * @param parser 页面解析器
	 * @param search_page (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception 
	 */
	public void extractLinks_google(Parser parser,int search_page) throws Exception{
		/* 设定符合谷歌的结果链接块过滤方式 并由此对解析器的内容进行过滤 */
		NodeFilter result_filter = new HasAttributeFilter("class", "g");
		NodeList nodes = parser.extractAllNodesThatMatch(result_filter);
		
		/* 对各个结果链接结点进行抓取过程 */
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); ++i) {
				// 逐个取出符合条件的链接结点
				Node resultNode = (Node) nodes.elementAt(i);
				
				if (((TagNode) resultNode).getAttribute("id") == null) {
					/*
					 * 正常情况 即纯<li class="g">的结点 其总个数应为一页10个
					 * 在正常情况中是不考虑新闻和图片这样的结点的 
					 * 如<li class="g" id="newsbox"> 或者是
					 * <li class="g" id="imagebox_bigimages"> 这样的结点不在正常情况中
					 */

					// 创建一个结果链接存储结构
					Result_Link_Struct result_link_struct = new Result_Link_Struct();

					// 获取链接所在原页号
					result_link_struct.setLink_page_from(search_page);
					result_link_struct.setLink_num_from((search_page - 1)*10 + i);

					
					/* 处理链接标题和链接URL(谷歌跳转URL) */
					// 有效的真正链接结点(包含标题和URL信息的最精确的结点)
					LinkTag effective_linktag = null;
					// 用以存储主链接结点是否常规的标志位
					boolean is_mainlink_regular = true;

					// 对结果结点中有几个<h3 class="r">...</h3>这样的有效连接结点进行分类讨论
					// mianklinkNode即是<h3 class="r">...</h3>结点
					NodeFilter linkclass_r = new HasAttributeFilter("class", "r");
					NodeList mainlinkNodeList = new NodeList();
					resultNode.collectInto(mainlinkNodeList, linkclass_r);
					Node mainlinkNode = null;
					if (mainlinkNodeList.size() != 0) {
						// 取第一个<h3 class="r">...</h3>
						mainlinkNode = mainlinkNodeList.elementAt(0);

						// 取出有效的真正链接结点effective_linktag
						// 此处所做的改变是能保证取出的一定是linktag 故不会发生强制类型转换出错的问题
						if (mainlinkNode.getFirstChild() instanceof LinkTag) {
							// 常规情况 能在第一个<h3 class="r">...</h3>的第一个子结点中取出linktag
							effective_linktag = (LinkTag) mainlinkNode.getFirstChild();
						} else {
							// 非常规情况 将是否常规的标志位置false 后转处理非常规的代码
							is_mainlink_regular = false;
						}
					} else {
						// 没有<h3 class="r">...</h3>的情况 为非常规主链接结点的情形
						// 将is_mainlink_regular标志位设为非常规
						is_mainlink_regular = false;
					}

					// 处理非常规的主链接结点的情形
					//直接取结果结点中的第一个linktag
					if (is_mainlink_regular == false) {
						NodeFilter link_Filter = new NodeClassFilter(LinkTag.class);
						NodeList linkList = new NodeList();
						resultNode.collectInto(linkList, link_Filter);
						effective_linktag = (LinkTag) linkList.elementAt(0);
					}

					// 存储链接标题
					result_link_struct.setLink_title(effective_linktag.getLinkText());
					
					//对谷歌跳转URL做处理 处理为实际的URL
					String link_url = effective_linktag.getLink();
					if(link_url.startsWith("http://www.google.com.hk/url?q=http://")){
						link_url = link_url.substring(31);
						link_url = link_url.substring(0,link_url.indexOf("&amp;"));
					}
					
					// 存储链接URL(此处为谷歌跳转URL)
					result_link_struct.setLink_url(link_url);
					
					/* 处理摘要 */
					NodeFilter abstractclass_stFilter = new HasAttributeFilter("class", "st");
					NodeList abstractNodeList = new NodeList();
					resultNode.collectInto(abstractNodeList, abstractclass_stFilter);
					Node abstractNode = null;
					if (abstractNodeList.size() != 0) {
						// 处理最简单的摘要模式 直接取第一个<span class="st">结点
						abstractNode = abstractNodeList.elementAt(0);
						String link_abstractString = abstractNode.toPlainTextString().replace("&ldquo;", "“")
								.replace("&rdquo;", "”").replace("&middot;", "·").replace("&nbsp;", " ")
								.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;","&")
								.replace("&quot;", "\"").replace(" ", "").replace("	", "");
						result_link_struct.setLink_abstract(link_abstractString);
					} else {
						// 处理各种非常规的摘要模式
						System.out.println("sorry. 第" + i + "个链接为非常规的摘要模式!");
						//////////尚未完成

					}
					
					/* 将该链接的信息块 顺序存入链接信息块队列中 */
					//目前设计为正常<li class="g">的结点才存入链接信息块队列
					result_links.add_link(result_link_struct);
				}else{
					/* 非通常情况
					 * 主要是google新闻和图片之类的集合链接
					 * 其不算在每页的10个搜索结果之中
					 * 故当存在新闻和图片集合链接时 nodes.size() > 10
					 * 该情况主要是类似这样的结点：
					 * <li class="g" id="newsbox"> 或  <li class="g" id="imagebox_bigimages"> 
					 */
					//////////尚未完成
//					System.out.println("not regular!");
				}
			}
		}
		// 对解析器进行重置 以便后续使用parser进行解析不会出错
		parser.reset();
		return;
	}
	
	/**
	 * 已过期不建议使用<br/>
	 * 对结果页面进行 各链接的标题 URL 摘要等信息进行抓取 的具体函数<br/>
	 * @version 1.0 能初步完成抓取任务 但百度部分异常较多 谷歌还未全部完成 未测试
	 * @param url (String) 将要被抓取的搜索结果页面的URL
	 * @param search_mode (char) 基础搜索引擎代码
	 * @param search_page (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception 抓取过程异常 主要是parser异常
	 */
	@Deprecated public void extractLinks_old(String url,char search_mode,int search_page) throws Exception {
		try {
			Parser parser = new Parser();
			
			parser.setEncoding("utf-8");

			/*代理设置 用于解决国内网络有时被墙的情况*/
			Parser.getConnectionManager().setProxyHost("127.0.0.1");
			Parser.getConnectionManager().setProxyPort(8118);
			
			parser.setURL(url);
			
			/*
			 * 对不同的基础搜索引擎所对应的结果页面
			 * 分情况进行信息抓取
			 */
			switch (search_mode) {
			case 'B':// 百度
				NodeFilter result_filter_regu = new HasAttributeFilter("class", "result");
				NodeFilter result_filter_op = new HasAttributeFilter("class", "result-op");
				OrFilter result_filter = new OrFilter(result_filter_regu,result_filter_op);
				NodeFilter linkclass_t = new HasAttributeFilter("class","t");
				NodeFilter result_child_filter = new HasParentFilter(result_filter, true);
				AndFilter result_link_filter = new AndFilter(linkclass_t,result_child_filter);

				NodeList nodes = parser.extractAllNodesThatMatch(linkclass_t);
				// NodeList nodes = parser.extractAllNodesThatMatch(result_filter);

				if (nodes != null) {
					for (int i = 0; i < nodes.size(); ++i) {

						// 逐个取出符合条件的链接结点
						Node textnode = (Node) nodes.elementAt(i);
						
						
						// 在链接结点中取得实际有效的链接结点
						Node effective_tag = textnode.getFirstChild();
						
						// 创建一个结果链接存储结构
						Result_Link_Struct result_link_struct = new Result_Link_Struct();
						//result_links.create_new_link();
						
						
						/* 抓取有效链接的链接标题 */
						String link_title = effective_tag.toPlainTextString();
						result_link_struct.setLink_title(link_title);
						//System.out.println(link_title);
						
						/* 抓取有效链接的URL */
						// 该URL为百度跳转URL
						if( effective_tag instanceof LinkTag ) {
							try{
								LinkTag effective_linktag = (LinkTag) effective_tag;
								//Parser parser1 = new Parser(effective_linktag.getLink());
								result_link_struct.setLink_url(effective_linktag.getLink());
							}catch(Exception e){
								continue;
							}
						}
						else {
							continue;
						}
						//result_links.get_last_link().link_url = effective_linktag.getLink();
					//	System.out.println(result_link_struct.link_url);
						
						
						/* 
						 * 在链接结点中找到最后百度快照等信息的结点部分
						 * cachemode表示是那一种快照格式类型 
						 * 0表示正常 即为textnode的后继的后继 同时也是其父结点的最后一个子女
						 * 1表示其为textnode的后继的后继 但不是其父结点的最后一个子女 但是最后第二个
						 * 2表示不能通过子女结点取到
						 * 3表示恰为随后一个  不是传统的子女结构
						*/
						int cachemode = 0;
						String cacheinfoString = "";
						Node cacheinfo;
						if(textnode.getNextSibling() == null)
						{
							continue;
						}
						if (!textnode.getNextSibling().toPlainTextString().contains("百度快照")){
							cachemode = 0;//正常情况
							cacheinfo = textnode.getParent().getLastChild();
							cacheinfoString = cacheinfo.toPlainTextString();
							
							if (!cacheinfo.toPlainTextString().contains("百度快照")) {
								if (cacheinfo.toPlainTextString().contains("查看更多关于")) {
									//为textnode的后继的后继 但不是其父结点的最后一个子女 但是最后第二个
									cachemode = 1;
									cacheinfo = cacheinfo.getPreviousSibling().getPreviousSibling();
									cacheinfoString = cacheinfo.toPlainTextString();
								} else {
									//表示不能通过子女结点取到 仅确定是最后几个结构中的一部分 通过字符串拼接得到快照等信息
									cachemode = 2;
									boolean has_cacheinfo = true;
									cacheinfo = textnode.getParent().getLastChild();
									while (!cacheinfo.getText().equals("span class=\"g\"")) {
										cacheinfo = cacheinfo.getPreviousSibling();
										if ((cacheinfo == textnode.getParent().getFirstChild())
												&& (!cacheinfo.getText().equals("span class=\"g\""))) {
											System.out.println("错误。没有百度快照tag");
											has_cacheinfo = false;
											break;
										}
									}
									if(cacheinfo.getNextSibling() != null){
										cacheinfoString = cacheinfo.toPlainTextString();
										cacheinfoString = cacheinfoString.concat(cacheinfo.getNextSibling().toPlainTextString())
												.concat(cacheinfo.getNextSibling().getNextSibling().toPlainTextString());
									}else{
										cacheinfoString = cacheinfo.toPlainTextString();
									}
								}
							}
						}else{
							//恰为随后一个  不是传统的子女结构
							cachemode = 3;
							cacheinfo = textnode.getNextSibling()
									.getLastChild();
							cacheinfo = cacheinfo.getPreviousSibling();
							cacheinfoString = cacheinfo.toPlainTextString();
						}

						
						// 抓取每个有效连接的描述性文字
						// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
						String describe_text = "";
						if(cachemode < 2){
							describe_text = textnode.getNextSibling().getNextSibling().toPlainTextString();
						}else if(cachemode == 3){
							describe_text = textnode.getNextSibling().getFirstChild().getNextSibling().toPlainTextString();
						}else{
							//System.out.println(textnode.getNextSibling().getFirstChild().getNextSibling().toPlainTextString());
							//此处不知道该怎么处理了。。。。
							describe_text = textnode.getParent().toPlainTextString().replace(link_title,"").replace(cacheinfoString,"");
						}
						//System.out.println(describe_text);
						result_link_struct.setLink_abstract(describe_text);
						
						//result_links.get_last_link().link_abstract = describe_text;
						
						
						//确定第几页
						result_link_struct.setLink_page_from(search_page);
						result_links.add_link(result_link_struct);
						//System.out.println("******==================******");
					}
				}
				break;

			case 'G':// google
				NodeFilter linkclass_r = new HasAttributeFilter("class","r");//用于google的链接结点过滤
				NodeFilter linkclass_st = new HasAttributeFilter("class","st");//用于google的链接描述文字结点过滤
				/*NodeFilter result_child_filter = new HasParentFilter(result_filter, true);
				AndFilter result_link_filter = new AndFilter(linkclass_t,result_child_filter);
				*/
				NodeList google_nodes_link = parser.extractAllNodesThatMatch(linkclass_r);
				NodeList google_nodes_describe = parser.extractAllNodesThatMatch(linkclass_st);

				//System.out.println("it's test_google");
				if (google_nodes_link != null) {
					System.out.println("have" + google_nodes_link.size());
					for (int i = 0; i < google_nodes_link.size(); ++i) {
						// 逐个取出符合条件的链接结点
						Node textnode = (Node) google_nodes_link.elementAt(i);

						// 在链接结点中取得实际有效的链接结点
						Node effective_tag = textnode.getFirstChild();
						
						// 创建一个结果链接存储结构
						Result_Link_Struct result_link_struct = new Result_Link_Struct();

						// 抓取有效链接的链接标题
					//	System.out.println(effective_tag.toPlainTextString());
						result_link_struct.setLink_title(effective_tag.toPlainTextString());
						
						// 抓取有效链接的URL
						LinkTag effective_linktag = (LinkTag) effective_tag;
						result_link_struct.setLink_url(effective_linktag.getLink());
					//	System.out.println(result_link_struct.link_url);
						
						// 抓取每个有效连接的描述性文字
						// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
						String describe_text = textnode.getParent().toPlainTextString();
					//	System.out.println(describe_text);
						result_link_struct.setLink_abstract(describe_text);
						
						result_links.add_link(result_link_struct);
					//	System.out.println("******==================******");
					}
				}
				break;
			default:
				throw new Exception("Illegal search mode!");
			}

		} catch (ParserException e) {
			e.printStackTrace();
			throw new Exception("Parser Exception!");
		}
		return;
	}
	
}
