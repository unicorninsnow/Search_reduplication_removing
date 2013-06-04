package crawl;


import java.util.Set;

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



public class Pages_analysis {
	final int text_para_threshold = 35;
	
	public void analyze_pages(Link_queue result_links){
		for(int i = 0;i < result_links.num_of_links();++i){
			//get_page_title(result_links.get_link(i));
			get_page_mainbody(result_links.get_link(i));
		}
		
		return;
	}
	
	/*public String get_page_title(String url_to_analyze) {
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
	}*/

	public void get_page_mainbody(Result_Link_Struct page_to_analyze) {
		String mainbody_analyzed = "";
		//System.out.println(page_to_analyze.getLink_title());
		//System.out.println(page_to_analyze.getLink_url());
		try {
			Parser parser = new Parser(page_to_analyze.getLink_url());
			NodeFilter mainbody_filter = new TagNameFilter("p");
			//NodeFilter mainbody_filter_p = new TagNameFilter("p");
			//NodeFilter mainbody_filter_article = new HasAttributeFilter("class", "article");
			//NodeFilter mainbody_filter = new OrFilter(mainbody_filter_p,mainbody_filter_article);
			NodeList main_body = parser.extractAllNodesThatMatch(mainbody_filter);

			if (main_body != null) {
				//System.out.println("main_body.size ===== " + main_body.size());
				for (int i = 0; i < main_body.size(); ++i) {
					Node mainbody_text = (Node) main_body.elementAt(i);
					String temptext = mainbody_text.toPlainTextString().replace("&ldquo;","“").replace("&rdquo;","”").replace("&middot;","·").replace("&nbsp", " ").replace(" ","").replace("	", "");
					if(temptext.length()> text_para_threshold){
						mainbody_analyzed = mainbody_analyzed + temptext + "\n"; 
						//System.out.println(temptext);
					}
					//System.out.println("=================paragraph=============");
				}
				//System.out.println(mainbody_analyzed);
				//System.out.println("=================one page is finished.==================");
				page_to_analyze.setLink_text(mainbody_analyzed);
			}
			else{
				System.out.println("why is title not exclusive?");
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return;
	}
}
