package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import datapackage.Result_Link_Struct;
import datapackage.Link_queue;

public class Search_engine_process {
	//如何处理Search_engine_process类和Pages_analysis类对result_links链表的共同使用问题
	Link_queue result_links = new Link_queue();
	

	public void extractLinks(String url,char search_mode,int Noofpagetoaccess) {
		try {
			Parser parser = new Parser();
			
			parser.setEncoding("utf-8");

			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
			parser.setURL(url);
			System.out.println(url);
			switch (search_mode) {
			case 'B':// 百度
				//parser.setEncoding("utf-8");
				NodeFilter result_filter_regu = new HasAttributeFilter("class", "result");
				NodeFilter result_filter_op = new HasAttributeFilter("class", "result-op");
				OrFilter result_filter = new OrFilter(result_filter_regu,result_filter_op);
				NodeFilter linkclass_t = new HasAttributeFilter("class","t");
				NodeFilter result_child_filter = new HasParentFilter(result_filter, true);
				AndFilter result_link_filter = new AndFilter(linkclass_t,result_child_filter);

				NodeList nodes = parser.extractAllNodesThatMatch(linkclass_t);
				// NodeList nodes = parser.extractAllNodesThatMatch(result_filter);

				//System.out.println("it's test_baidu");
				if (nodes != null) {
					for (int i = 0; i < nodes.size(); ++i) {

						// 逐个取出符合条件的链接结点
						Node textnode = (Node) nodes.elementAt(i);
						
						
						// 在链接结点中取得实际有效的链接结点
						Node effective_tag = textnode.getFirstChild();
						
						// 创建一个结果链接存储结构
						Result_Link_Struct result_link_struct = new Result_Link_Struct();
						//result_links.create_new_link();
						
						
						// 抓取有效链接的链接标题
						String link_title = effective_tag.toPlainTextString();
						result_link_struct.setLink_title(link_title);
						System.out.println(link_title);
						
						// 抓取有效链接的URL
						// 该URL为百度跳转URL
						if( effective_tag instanceof LinkTag ) {
							try{
								LinkTag effective_linktag = (LinkTag) effective_tag;
								Parser parser1 = new Parser(effective_linktag.getLink());
								result_link_struct.setLink_url(parser1.getURL());
							}catch(Exception e){
								break;
							}
						}
						else {
							break;
						}
						//result_links.get_last_link().link_url = effective_linktag.getLink();
					//	System.out.println(result_link_struct.link_url);
						
						
						
						// 在链接结点中找到最后百度快照等信息的结点部分
						//cachemode表示是那一种快照格式类型 
						//0表示正常 即为textnode的后继的后继 同时也是其父结点的最后一个子女
						//1表示其为textnode的后继的后继 但不是其父结点的最后一个子女 但是最后第二个
						//2表示不能通过子女结点取到
						//3表示恰为随后一个  不是传统的子女结构
						int cachemode = 0;
						String cacheinfoString = "";
						Node cacheinfo;
						System.out.println(textnode.getNextSibling().toPlainTextString());
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
									cacheinfo = textnode.getParent().getLastChild();
									while (!cacheinfo.getText().equals("span class=\"g\"")) {
										cacheinfo = cacheinfo.getPreviousSibling();
										if ((cacheinfo == textnode.getParent().getFirstChild())
												&& (!cacheinfo.getText().equals("span class=\"g\""))) {
											System.out.println("错误。没有百度快照tag");
											break;
										}
									}
									cacheinfoString = cacheinfo.toPlainTextString();
									cacheinfoString = cacheinfoString.concat(cacheinfo.getNextSibling().toPlainTextString())
											.concat(cacheinfo.getNextSibling().getNextSibling().toPlainTextString());
									//System.out.println(cacheinfoString);
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
						System.out.println(cachemode);
						System.out.println(cacheinfoString);

						
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
						System.out.println("******==================******");
					}
				}
				break;

			case 'G':// google
				//parser.setEncoding("gb2312");
				
				NodeFilter linkclass_r = new HasAttributeFilter("class","r");//用于google的链接结点过滤
				NodeFilter linkclass_st = new HasAttributeFilter("class","st");//用于google的链接描述文字结点过滤
				/*NodeFilter result_child_filter = new HasParentFilter(
						result_filter, true);
				AndFilter result_link_filter = new AndFilter(linkclass_t,
						result_child_filter);
				*/
				NodeList google_nodes_link = parser
						.extractAllNodesThatMatch(linkclass_r);
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
				System.out.println("errorrrrrrrrr");
			}

		} catch (ParserException e) {
			e.printStackTrace();
		}
		// return result_links;
		return;
	}

	public Link_queue getresult_links(){
		return result_links;
	}
}
