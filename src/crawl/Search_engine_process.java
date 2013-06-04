package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Result_Link_Struct;
import datapackage.Link_queue;

public class Search_engine_process {
	//如何处理Search_engine_process类和Pages_analysis类对result_links链表的共同使用问题
	Link_queue result_links = new Link_queue();
	

	public void extractLinks(String url,char search_mode,int Noofpagetoaccess/*,Set<String> result_links*/) {
		//Set<String> result_links = new HashSet<String>();
		try {
			
			Parser parser = new Parser();
			
			parser.setEncoding("utf-8");

			parser.getConnectionManager().setProxyHost("127.0.0.1");
			parser.getConnectionManager().setProxyPort(8118);
			
			parser.setURL(url);
			
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
				// NodeList nodes =
				// parser.extractAllNodesThatMatch(result_filter);

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
					//	System.out.println(effective_tag.toPlainTextString());
						result_link_struct.setLink_title(effective_tag.toPlainTextString());
						//result_links.get_last_link().link_title = effective_tag.toPlainTextString();

						// 抓取有效链接的URL
						LinkTag effective_linktag = (LinkTag) effective_tag;
						result_link_struct.setLink_url(effective_linktag.getLink());
						//result_links.get_last_link().link_url = effective_linktag.getLink();
					//	System.out.println(result_link_struct.link_url);
						
						// 抓取每个有效连接的描述性文字
						// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
						String describe_text = textnode.getParent()
								.toPlainTextString();
					//	System.out.println(describe_text);
						result_link_struct.setLink_abstract(describe_text);
						//result_links.get_last_link().link_abstract = describe_text;
						
						
						//确定第几页
						result_link_struct.setLink_page_from(Noofpagetoaccess);
						
						result_links.add_link(result_link_struct);
					//	System.out.println("******==================******");
					}
				}
				break;

			case 'G':// google
				//parser.setEncoding("gb2312");
				
				//DownLoadFile downloadhtml;
				//downloadhtml.downloadFile(url);
				
				
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
