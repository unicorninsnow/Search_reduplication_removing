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
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import datapackage.Result_Link_Struct;
import datapackage.Link_queue;

/**
 * 该类为分析搜索结果页面的类<br/>
 * 将各个结果链接的标题 URL 摘要等信息 抓取到相应的链接信息块队列中
 * @author Daniel
 * @version 1.1
 */
public class Search_engine_process {
	//////////如何处理Search_engine_process类和Pages_analysis类对result_links链表的共同使用问题
	/**链接信息块队列<br/>典型长度值为10n*/
	Link_queue result_links = new Link_queue();
	
	/**
	 * 对结果页面进行 各链接的标题 URL 摘要等信息进行抓取 的具体函数
	 * @param url (String) 将要被抓取的搜索结果页面的URL
	 * @param search_mode (char) 基础搜索引擎代码
	 * @param Noofpagetoaccess (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception 抓取过程异常 主要是parser异常
	 */
	public void extractLinks(String url,char search_mode,int Noofpagetoaccess) throws Exception {
		try {
			Parser parser = new Parser();
			
			parser.setEncoding("utf-8");

			/*代理设置 用于解决国内网络有时被墙的情况*/
			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
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
						LinkTag effective_linktag = (LinkTag) effective_tag;
						result_link_struct.setLink_url(effective_linktag.getLink());
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
									if(has_cacheinfo){
										cacheinfoString = cacheinfo.toPlainTextString();
										cacheinfoString = cacheinfoString.concat(cacheinfo.getNextSibling().toPlainTextString())
												.concat(cacheinfo.getNextSibling().getNextSibling().toPlainTextString());
									}else{
										cacheinfoString = "";
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
						result_link_struct.setLink_page_from(Noofpagetoaccess);
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

	/**
	 * 测试用 仅测试百度<br/> 
	 * 对结果页面进行 各链接的标题 URL 摘要等信息进行抓取 的具体函数
	 * @param url (String) 将要被抓取的搜索结果页面的URL
	 * @param search_mode (char) 基础搜索引擎代码
	 * @param Noofpagetoaccess (int) 结果页面的页号(是对应链接的信息 用于存入信息块)
	 * @throws Exception 抓取过程异常 主要是parser异常
	 */
	public void test_extractLinks(String url,char search_mode,int Noofpagetoaccess) throws Exception {
		try {
			Parser parser = new Parser();
			
			parser.setEncoding("utf-8");

			/*代理设置 用于解决国内网络有时被墙的情况*/
			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
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
				
				NodeFilter trFilter = new TagNameFilter("tr");
				NodeFilter linkclass_t = new HasAttributeFilter("class","t");
				NodeFilter result_child_filter = new HasParentFilter(result_filter, true);
				AndFilter result_link_filter = new AndFilter(linkclass_t,result_child_filter);

				NodeList nodes = parser.extractAllNodesThatMatch(result_filter);
				// NodeList nodes = parser.extractAllNodesThatMatch(result_filter);

				if (nodes != null) {
					for (int i = 0; i < nodes.size(); ++i) {
						// 创建一个结果链接存储结构
						Result_Link_Struct result_link_struct = new Result_Link_Struct();
						
						
						// 逐个取出符合条件的链接结点
						Node resultNode = (Node) nodes.elementAt(i);
						//////////暂未处理result-op的情况
						
//						System.out.println(resultNode.toHtml());
						System.out.println(resultNode.getText());
						
						

						//获取结果块的第一个tr结点 trNode   即有效内容的tr结点
						NodeList trNodeList = new NodeList();
						resultNode.collectInto(trNodeList, trFilter);
						if (trNodeList.size() == 0)	throw new Exception("no tr tag.errrrrrrrrrrr.");
						Node trNode = trNodeList.elementAt(0);
						
						//对主tr结点中有几个<h3 class="t">...</h3>这样的有效连接结点进行分类讨论
						//mianklinkNode即是<h3 class="t">...</h3>结点
						NodeList mainlinkNodeList = new NodeList();
						trNode.collectInto(mainlinkNodeList, linkclass_t);
						Node mainlinkNode = null;
						if(mainlinkNodeList.size() == 0){
							//处理没有<h3 class="t">...</h3>的情况
						}else{
							//取第一个<h3 class="t">...</h3>
							mainlinkNode = mainlinkNodeList.elementAt(0);
						}
						
						System.out.println("主链接结点标题：" + mainlinkNode.toPlainTextString());
						
						if(mainlinkNode.getFirstChild() instanceof LinkTag){
							System.out.println("YYYYYYES! It's first child.");
							LinkTag effective_linktag = (LinkTag) mainlinkNode.getFirstChild();
							result_link_struct.setLink_url(effective_linktag.getLinkText());
							System.out.println("linktitle = " + effective_linktag.getLinkText());
							result_link_struct.setLink_url(effective_linktag.getLink()); 
							System.out.println("linkurl = " + effective_linktag.getLink());

						}else{
							System.out.println("NNNNNNO! It's not the first child.");
							NodeFilter link_Filter = new NodeClassFilter(LinkTag.class);
							NodeList linkList = new NodeList();
							trNode.collectInto(linkList, link_Filter);
							System.out.println(linkList.size());
							LinkTag effective_linktag = (LinkTag) linkList.elementAt(0);
							result_link_struct.setLink_url(effective_linktag.getLinkText());
							System.out.println("linktitle = " + effective_linktag.getLinkText());
							result_link_struct.setLink_url(effective_linktag.getLink());
							System.out.println("linkurl = " + effective_linktag.getLink());
							
							
						}
						
//						Node trNode = tbodyNode.elementAt(0);
//						NodeList trchildrenList = trNode.getChildren();
//						System.out.println("children size = " + trchildrenList.elementAt(0).getText());
//						
						
						
						
						System.out.println("==========================================================");
						
						
						
						
//						
//						// 在链接结点中取得实际有效的链接结点
//						Node effective_tag = textnode.getFirstChild();
//						
//						// 创建一个结果链接存储结构
//						Result_Link_Struct result_link_struct = new Result_Link_Struct();
//						//result_links.create_new_link();
//						
//						
//						/* 抓取有效链接的链接标题 */
//						String link_title = effective_tag.toPlainTextString();
//						result_link_struct.setLink_title(link_title);
//						//System.out.println(link_title);
//						
//						/* 抓取有效链接的URL */
//						// 该URL为百度跳转URL
//						LinkTag effective_linktag = (LinkTag) effective_tag;
//						result_link_struct.setLink_url(effective_linktag.getLink());
//						//result_links.get_last_link().link_url = effective_linktag.getLink();
//					//	System.out.println(result_link_struct.link_url);
//						
//						
//						/* 
//						 * 在链接结点中找到最后百度快照等信息的结点部分
//						 * cachemode表示是那一种快照格式类型 
//						 * 0表示正常 即为textnode的后继的后继 同时也是其父结点的最后一个子女
//						 * 1表示其为textnode的后继的后继 但不是其父结点的最后一个子女 但是最后第二个
//						 * 2表示不能通过子女结点取到
//						 * 3表示恰为随后一个  不是传统的子女结构
//						*/
//						int cachemode = 0;
//						String cacheinfoString = "";
//						Node cacheinfo;
//						if (!textnode.getNextSibling().toPlainTextString().contains("百度快照")){
//							cachemode = 0;//正常情况
//							cacheinfo = textnode.getParent().getLastChild();
//							cacheinfoString = cacheinfo.toPlainTextString();
//							
//							if (!cacheinfo.toPlainTextString().contains("百度快照")) {
//								if (cacheinfo.toPlainTextString().contains("查看更多关于")) {
//									//为textnode的后继的后继 但不是其父结点的最后一个子女 但是最后第二个
//									cachemode = 1;
//									cacheinfo = cacheinfo.getPreviousSibling().getPreviousSibling();
//									cacheinfoString = cacheinfo.toPlainTextString();
//								} else {
//									//表示不能通过子女结点取到 仅确定是最后几个结构中的一部分 通过字符串拼接得到快照等信息
//									cachemode = 2;
//									boolean has_cacheinfo = true;
//									cacheinfo = textnode.getParent().getLastChild();
//									while (!cacheinfo.getText().equals("span class=\"g\"")) {
//										cacheinfo = cacheinfo.getPreviousSibling();
//										if ((cacheinfo == textnode.getParent().getFirstChild())
//												&& (!cacheinfo.getText().equals("span class=\"g\""))) {
//											System.out.println("错误。没有百度快照tag");
//											has_cacheinfo = false;
//											break;
//										}
//									}
//									if(has_cacheinfo){
//										cacheinfoString = cacheinfo.toPlainTextString();
//										cacheinfoString = cacheinfoString.concat(cacheinfo.getNextSibling().toPlainTextString())
//												.concat(cacheinfo.getNextSibling().getNextSibling().toPlainTextString());
//									}else{
//										cacheinfoString = "";
//									}
//								}
//							}
//						}else{
//							//恰为随后一个  不是传统的子女结构
//							cachemode = 3;
//							cacheinfo = textnode.getNextSibling()
//									.getLastChild();
//							cacheinfo = cacheinfo.getPreviousSibling();
//							cacheinfoString = cacheinfo.toPlainTextString();
//						}
//
//						
//						// 抓取每个有效连接的描述性文字
//						// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
//						String describe_text = "";
//						if(cachemode < 2){
//							describe_text = textnode.getNextSibling().getNextSibling().toPlainTextString();
//						}else if(cachemode == 3){
//							describe_text = textnode.getNextSibling().getFirstChild().getNextSibling().toPlainTextString();
//						}else{
//							//System.out.println(textnode.getNextSibling().getFirstChild().getNextSibling().toPlainTextString());
//							//此处不知道该怎么处理了。。。。
//							describe_text = textnode.getParent().toPlainTextString().replace(link_title,"").replace(cacheinfoString,"");
//						}
//						//System.out.println(describe_text);
//						result_link_struct.setLink_abstract(describe_text);
//						
//						//result_links.get_last_link().link_abstract = describe_text;
//						
//						
//						//确定第几页
//						result_link_struct.setLink_page_from(Noofpagetoaccess);
//						result_links.add_link(result_link_struct);
//						//System.out.println("******==================******");
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
	
	public Link_queue getresult_links(){
		return result_links;
	}
}
